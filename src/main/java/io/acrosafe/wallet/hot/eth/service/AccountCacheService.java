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

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import io.acrosafe.wallet.hot.eth.config.ApplicationProperties;
import io.acrosafe.wallet.hot.eth.domain.AccountRecord;
import io.acrosafe.wallet.hot.eth.repository.AccountRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.acrosafe.wallet.core.eth.ETHAccount;
import io.acrosafe.wallet.core.eth.exception.AccountNotFoundException;

@Service
public class AccountCacheService
{
    // Logger
    private static final Logger logger = LoggerFactory.getLogger(AccountCacheService.class);

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private AccountRecordRepository accountRecordRepository;

    private Map<String, ETHAccount> accounts = new ConcurrentHashMap<>();

    @PostConstruct
    public void initialize()
    {
        try
        {
            restoreAccounts();
        }
        catch (Throwable t)
        {
            logger.error("failed to load accounts.", t);
        }
    }

    public void addAccountToCache(String accountId, ETHAccount account)
    {
        accounts.put(accountId, account);
    }

    public ETHAccount getAccount(String accountId) throws AccountNotFoundException
    {
        ETHAccount account = this.accounts.get(accountId);
        if (account == null)
        {
            throw new AccountNotFoundException("failed to find enterprise account " + accountId);
        }

        return account;
    }

    private void restoreAccounts()
    {
        List<AccountRecord> accountRecords = this.accountRecordRepository.findAllByEnabledTrue();

        if (accountRecords != null && accountRecords.size() != 0)
        {
            for (AccountRecord accountRecord : accountRecords)
            {
                if (accountRecord.isEnabled())
                {
                    final String encryptedSeed = accountRecord.getSeed();
                    final byte[] spec = Base64.getDecoder().decode(accountRecord.getSpec());
                    final byte[] salt = Base64.getDecoder().decode(accountRecord.getSalt());
                    final String address = accountRecord.getAddress();
                    ETHAccount account = new ETHAccount(encryptedSeed, spec, salt, applicationProperties.getTestnet(), address);

                    this.accounts.put(accountRecord.getId(), account);
                    logger.info("restored enterprise account {}.", accountRecord.getId());
                }
            }
        }
    }
}
