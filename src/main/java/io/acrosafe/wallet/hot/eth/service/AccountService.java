package io.acrosafe.wallet.hot.eth.service;

import io.acrosafe.wallet.core.eth.CryptoUtils;
import io.acrosafe.wallet.core.eth.ETHAccount;
import io.acrosafe.wallet.core.eth.IDGenerator;
import io.acrosafe.wallet.core.eth.SeedGenerator;
import io.acrosafe.wallet.core.eth.SignedTransaction;
import io.acrosafe.wallet.core.eth.exception.AccountNotFoundException;
import io.acrosafe.wallet.core.eth.exception.ContractCreationException;
import io.acrosafe.wallet.core.eth.exception.CryptoException;
import io.acrosafe.wallet.core.eth.exception.InvalidCredentialException;
import io.acrosafe.wallet.hot.eth.config.ApplicationProperties;
import io.acrosafe.wallet.hot.eth.domain.AccountRecord;
import io.acrosafe.wallet.hot.eth.domain.AddressRecord;
import io.acrosafe.wallet.hot.eth.exception.InvalidCoinSymbolException;
import io.acrosafe.wallet.hot.eth.repository.AccountRecordRepository;
import io.acrosafe.wallet.hot.eth.repository.AddressRecordRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.protocol.core.methods.response.EthSendTransaction;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class AccountService
{
    // Logger
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    private static final String ETH_SYMBOL = "ETH";

    @Autowired
    private SeedGenerator seedGenerator;

    @Autowired
    private BlockChainService blockChainService;

    @Autowired
    private AccountCacheService accountCacheService;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private AccountRecordRepository accountRecordRepository;

    @Autowired
    private AddressRecordRepository addressRecordRepository;

    @PostConstruct
    public void initialize()
    {
        addBlockChainListener();
    }

    /**
     * Creates new enterprise account based on given symbol and label.
     *
     * @param label
     * @param symbol
     * @return
     * @throws CryptoException
     * @throws InvalidCredentialException
     * @throws InvalidCoinSymbolException
     */
    @Transactional
    public AccountRecord createAccount(String symbol, String label, Boolean enabled)
            throws CryptoException, InvalidCoinSymbolException
    {
        if (StringUtils.isEmpty(symbol) || !symbol.equalsIgnoreCase(ETH_SYMBOL))
        {
            throw new InvalidCoinSymbolException("coin symbol is not valid.");
        }

        if (enabled == null)
        {
            enabled = true;
        }

        final byte[] seed = this.seedGenerator.getSeed(this.applicationProperties.getServiceId(), 256, 256);

        final byte[] spec = CryptoUtils.generateIVParameterSpecBytes();
        final String encodedSpec = Base64.getEncoder().encodeToString(spec);
        final byte[] ownerSalt = CryptoUtils.generateSaltBytes();
        final String encodedOwnerSalt = Base64.getEncoder().encodeToString(ownerSalt);

        String encryptedSeed = null;
        try
        {
            encryptedSeed =
                    CryptoUtils.encrypt(this.applicationProperties.getPassphrase().getStringValue(), seed, spec, ownerSalt);
        }
        catch (Throwable t)
        {
            // this shouldn't happen at all.
            throw new CryptoException("Invalid crypto operation.", t);
        }

        final String id = IDGenerator.randomUUID().toString();
        ETHAccount account = new ETHAccount(encryptedSeed, spec, ownerSalt, this.applicationProperties.getTestnet(),
                this.applicationProperties.getPassphrase());

        AccountRecord enterpriseAccountRecord = new AccountRecord();
        enterpriseAccountRecord.setId(id);
        enterpriseAccountRecord.setLabel(label);
        enterpriseAccountRecord.setEnabled(true);
        enterpriseAccountRecord.setSeed(encryptedSeed);
        enterpriseAccountRecord.setSpec(encodedSpec);
        enterpriseAccountRecord.setSalt(encodedOwnerSalt);
        enterpriseAccountRecord.setAddress(account.getAddress());
        enterpriseAccountRecord.setCreatedDate(Instant.now());

        this.accountRecordRepository.save(enterpriseAccountRecord);

        this.accountCacheService.addAccountToCache(id, account);

        return enterpriseAccountRecord;
    }

    @Transactional
    public String createReceivingAddress(String symbol, String label, String accountId)
            throws ContractCreationException, AccountNotFoundException, InvalidCoinSymbolException
    {
        if (StringUtils.isEmpty(symbol) || !symbol.equalsIgnoreCase(ETH_SYMBOL))
        {
            throw new InvalidCoinSymbolException("coin symbol is not valid.");
        }
        ETHAccount account = this.accountCacheService.getAccount(accountId);

        final String id = IDGenerator.randomUUID().toString();
        AddressRecord addressRecord = new AddressRecord();
        addressRecord.setId(id);
        addressRecord.setAccountId(accountId);
        addressRecord.setCreatedDate(Instant.now());
        if (!StringUtils.isEmpty(label))
        {
            addressRecord.setLabel(label);
        }
        this.addressRecordRepository.save(addressRecord);

        logger.info("new address record {} is created.", id);
        this.blockChainService.deployAddressContract(id, account.getCredentials(this.applicationProperties.getPassphrase()),
                account.getAddress());

        return id;
    }

    @Transactional
    public List<AccountRecord> getAccounts(int pageId, int size)
    {
        Pageable pageable = PageRequest.of(pageId, size, Sort.by(Sort.Direction.ASC, "CreatedDate"));
        return this.accountRecordRepository.findAllByEnabledTrue(pageable);
    }

    @Transactional
    public String getAccountAddress(String accountId) throws AccountNotFoundException
    {
        ETHAccount account = this.accountCacheService.getAccount(accountId);

        return account.getAddress();
    }

    public Map<String, BigInteger> getBalances(String accountId) throws AccountNotFoundException
    {
        final ETHAccount account = this.accountCacheService.getAccount(accountId);

        return this.blockChainService.getBalances(account.getAddress(), null);
    }

    private void addBlockChainListener()
    {
        List<AddressRecord> addressRecords = this.addressRecordRepository.findAll();
        if (addressRecords != null && addressRecords.size() != 0)
        {
            for (AddressRecord addressRecord : addressRecords)
            {
                String address = addressRecord.getAddress();
                if (address != null)
                {
                    logger.info("restoring transactions for address {}", addressRecord.getAddress());
                    try
                    {
                        final String accountId = addressRecord.getAccountId();
                        this.blockChainService.subscribeToEtherEvent(address, accountId);
                    }
                    catch (Throwable t)
                    {
                        // we will let it continue.
                        logger.error("failed to get deposit history for address {}", address, t);
                    }
                }
                else
                {
                    logger.info("address record {} doesn't have valid address.", address);
                }
            }
        }
    }

    public synchronized String send(String symbol, String accountId, String address, String amount, String internalTransactionId)
            throws InvalidCoinSymbolException, AccountNotFoundException
    {
        if (StringUtils.isEmpty(symbol) || !symbol.equalsIgnoreCase(ETH_SYMBOL))
        {
            throw new InvalidCoinSymbolException("coin symbol is not valid.");
        }

        ETHAccount account = this.accountCacheService.getAccount(accountId);

        SignedTransaction signedTransaction = this.blockChainService.buildAndSignTransaction(accountId, account, address, amount, this.applicationProperties.getPassphrase());
        logger.info("transaction signed. hex = {}", signedTransaction);

        return this.blockChainService.send(signedTransaction);
    }
}
