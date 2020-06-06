/**
 * MIT License
 *
 * Copyright (c) 2020 acrosafe technologies
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.acrosafe.wallet.hot.eth.service;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import io.acrosafe.wallet.core.eth.ETHAccount;
import io.acrosafe.wallet.core.eth.Passphrase;
import io.acrosafe.wallet.core.eth.SignedTransaction;
import io.acrosafe.wallet.hot.eth.domain.AddressRecord;
import io.acrosafe.wallet.hot.eth.domain.TransactionRecord;
import io.acrosafe.wallet.hot.eth.repository.AddressRecordRepository;
import io.acrosafe.wallet.hot.eth.repository.TransactionRecordRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import io.acrosafe.wallet.core.eth.BlockChainNetwork;
import io.acrosafe.wallet.core.eth.IDGenerator;
import io.acrosafe.wallet.core.eth.Token;
import io.acrosafe.wallet.core.eth.TransactionStatus;
import io.acrosafe.wallet.core.eth.TransactionType;
import io.acrosafe.wallet.core.eth.TransactionUtils;
import io.acrosafe.wallet.core.eth.exception.ContractCreationException;

@Service
public class BlockChainService
{
    // Logger
    private static final Logger logger = LoggerFactory.getLogger(BlockChainService.class);

    private static final String DEFAULT_HASH_STRING = "0x0";

    @Autowired
    private BlockChainNetwork blockChainNetwork;

    @Autowired
    private AddressRecordRepository addressRecordRepository;

    @Autowired
    private TransactionRecordRepository transactionRecordRepository;

    public synchronized Map<String, BigInteger> getBalances(String accountAddress, List<Token> tokens)
    {
        return blockChainNetwork.getBalance(accountAddress, tokens);
    }

    @Async
    public synchronized void subscribeToEtherEvent(String address, String accountId)
    {
        this.blockChainNetwork.getETHFilter(address).subscribe(log -> {
            final String hash = log.getTransactionHash();
            logger.info("=================================== hash is : {}", hash);
            try
            {
                EthTransaction transaction = this.blockChainNetwork.getTransactionByHash(hash);
                EthGetTransactionReceipt receipt = this.blockChainNetwork.getTransactionReceiptByHash(hash);
                TransactionReceipt transactionReceipt = receipt.getTransactionReceipt().orElse(null);
                TransactionStatus status = TransactionUtils.getTransactionStatus(transactionReceipt);

                TransactionRecord existingTransactionRecord =
                        this.transactionRecordRepository.findFirstByTransactionId(hash).orElse(null);
                if (existingTransactionRecord == null)
                {
                    TransactionRecord transactionRecord = new TransactionRecord();
                    transactionRecord.setId(IDGenerator.randomUUID().toString());
                    transactionRecord.setStatus(status);
                    transactionRecord.setAmount(transaction.getResult().getValue());
                    transactionRecord.setCreatedDate(Instant.now());
                    transactionRecord.setFee(BigInteger.ZERO);
                    transactionRecord.setLastModifiedDate(Instant.now());
                    transactionRecord.setTransactionId(hash);
                    transactionRecord.setTransactionType(TransactionType.DEPOSIT);
                    transactionRecord.setAccountId(accountId);
                    transactionRecord.setToken("ETH");
                    transactionRecord.setDestination(address);

                    this.transactionRecordRepository.save(transactionRecord);
                    logger.info("found new deposite {} for address {} for eth. value = {}, status = {}", hash, address,
                            transaction.getResult().getValue(), status);
                }
                else
                {
                    if (existingTransactionRecord.getStatus() != TransactionStatus.CONFIRMED)
                    {
                        existingTransactionRecord.setStatus(status);
                        this.transactionRecordRepository.save(existingTransactionRecord);
                        logger.info("updated existing transaction status for eth. hash = {}, address = {}, status = {}", hash,
                                address, status);
                    }
                    else
                    {
                        logger.info("found existing deposit record for eth. hash = {}, address = {}, status = {}, value = {}",
                                hash, address, status, existingTransactionRecord.getAmount());
                    }
                }
            }
            catch (Throwable t)
            {
                // we have to let it go
                logger.warn("failed to add listener to address {}", address, t);
            }
        });
    }

    @Async
    public synchronized void deployAddressContract(String addressId, Credentials credentials, String ownerAccountAddress)
            throws ContractCreationException
    {
        try
        {
            // TODO: need remove hardcoded gas value
            String contractAddress = this.blockChainNetwork.deployAddressContractWithDefaultParent(credentials,
                    BigInteger.valueOf(12_000_000_000L), BigInteger.valueOf(2300000));
            if (!StringUtils.isEmpty(contractAddress))
            {
                AddressRecord record = this.addressRecordRepository.findById(addressId).orElse(null);
                if (record != null)
                {
                    record.setAddress(contractAddress);
                    this.addressRecordRepository.save(record);

                    subscribeToEtherEvent(contractAddress, record.getAccountId());

                    logger.info(
                            "address {} has been deployed to blockchain and persisted into DB. contract address = {}, owner account address = {}",
                            addressId, contractAddress, ownerAccountAddress);
                }
                else
                {
                    // this is almost impossible
                    logger.warn("failed to find address {} in DB.", addressId);
                }
            }
            else
            {
                throw new ContractCreationException("address contract is not valid.");
            }
        }
        catch (Throwable t)
        {
            throw new ContractCreationException("failed to deploy address sub-contract on blockchain.", t);
        }
    }

    @Transactional
    public SignedTransaction buildAndSignTransaction(String accountId, ETHAccount account, String address, String amount,
            Passphrase passphrase)
    {
        final String id = IDGenerator.randomUUID().toString();
        TransactionRecord transactionRecord = new TransactionRecord();
        transactionRecord.setId(id);
        transactionRecord.setStatus(TransactionStatus.SIGNED);
        transactionRecord.setAmount(new BigInteger(amount));
        transactionRecord.setCreatedDate(Instant.now());
        transactionRecord.setFee(BigInteger.ZERO);
        transactionRecord.setLastModifiedDate(Instant.now());
        transactionRecord.setTransactionId(DEFAULT_HASH_STRING);
        transactionRecord.setTransactionType(TransactionType.WITHDRAWAL);
        transactionRecord.setAccountId(accountId);
        transactionRecord.setToken("ETH");
        transactionRecord.setDestination(address);

        this.transactionRecordRepository.save(transactionRecord);

        SignedTransaction signedTransaction = new SignedTransaction();
        signedTransaction.setId(id);
        signedTransaction.setHex(this.blockChainNetwork.buildAndSign(account, address, amount,
                BigInteger.valueOf(20_000_000_000L), BigInteger.valueOf(100000), passphrase));

        return signedTransaction;
    }

    public String send(SignedTransaction signedTransaction)
    {
        EthSendTransaction transactionResponse = this.blockChainNetwork.sendSignedTransaction(signedTransaction.getHex());
        if (transactionResponse.hasError())
        {
            logger.warn("found error in transaction response. error code = {}, error message = {}",
                    transactionResponse.getError().getCode(), transactionResponse.getError().getMessage());
        }

        TransactionRecord record = this.transactionRecordRepository.findById(signedTransaction.getId()).orElse(null);

        String hash = transactionResponse.getTransactionHash();
        if (StringUtils.isEmpty(hash))
        {
            record.setStatus(TransactionStatus.FAILED);
        }
        else
        {
            EthGetTransactionReceipt receipt = this.blockChainNetwork.getTransactionReceiptByHash(hash);
            TransactionReceipt transactionReceipt = receipt.getTransactionReceipt().orElse(null);
            TransactionStatus status = TransactionUtils.getTransactionStatus(transactionReceipt);
            Transaction transaction = this.blockChainNetwork.getTransactionByHash(hash).getTransaction().orElse(null);
            if (transaction != null)
            {
                final BigInteger gas = transaction.getGas();
                final BigInteger price = transaction.getGasPrice();
                record.setFee(gas.multiply(price));
                record.setStatus(status);

                this.transactionRecordRepository.save(record);
                logger.info("updated transaction status. status = {}, fee = {}", status, gas);
            }

        }

        return hash;

    }
}
