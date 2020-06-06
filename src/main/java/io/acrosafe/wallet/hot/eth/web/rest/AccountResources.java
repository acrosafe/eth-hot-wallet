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
package io.acrosafe.wallet.hot.eth.web.rest;

import io.acrosafe.wallet.core.eth.exception.ContractCreationException;
import io.acrosafe.wallet.core.eth.exception.WalletNotFoundException;
import io.acrosafe.wallet.hot.eth.domain.AccountRecord;
import io.acrosafe.wallet.hot.eth.exception.InvalidCoinSymbolException;
import io.acrosafe.wallet.hot.eth.exception.ServiceNotReadyException;
import io.acrosafe.wallet.hot.eth.service.AccountService;
import io.acrosafe.wallet.hot.eth.web.rest.request.CreateAccountRequest;
import io.acrosafe.wallet.hot.eth.web.rest.request.GetReceiveAddressRequest;
import io.acrosafe.wallet.hot.eth.web.rest.request.SendCoinRequest;
import io.acrosafe.wallet.hot.eth.web.rest.response.CreateAccountResponse;
import io.acrosafe.wallet.hot.eth.web.rest.response.GetAddressResponse;
import io.acrosafe.wallet.hot.eth.web.rest.response.GetAllTokenBalancesResponse;
import io.acrosafe.wallet.hot.eth.web.rest.response.GetBalanceResponse;
import io.acrosafe.wallet.hot.eth.web.rest.response.Result;
import io.acrosafe.wallet.hot.eth.web.rest.response.SendCoinResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.acrosafe.wallet.core.eth.exception.AccountNotFoundException;
import io.acrosafe.wallet.core.eth.exception.CryptoException;

import java.math.BigInteger;
import java.util.Map;

@Controller
@RequestMapping("/api/v1/eth/wallet")
public class AccountResources
{
    // Logger
    private static final Logger logger = LoggerFactory.getLogger(AccountResources.class);

    @Autowired
    private AccountService service;

    @PostMapping("/{walletId}/address/new")
    public ResponseEntity<GetAddressResponse> createReceivingAddress(@PathVariable String walletId,
            @RequestBody GetReceiveAddressRequest request)
    {
        GetAddressResponse response = new GetAddressResponse();
        try
        {
            String id = this.service.createReceivingAddress(request.getSymbol(), request.getLabel(), walletId);
            response.setId(id);

            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        }
        catch (AccountNotFoundException e)
        {
            response.setResultCode(Result.ACCOUNT_NOT_FOUND.getCode());
            response.setResult(Result.WALLET_NOT_FOUND);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        catch (ContractCreationException e)
        {
            response.setResultCode(Result.CREATE_CONTRACT_FAILED.getCode());
            response.setResult(Result.CREATE_CONTRACT_FAILED);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        catch (Throwable t)
        {
            logger.error("failed to create new address.", t);
            response.setResultCode(Result.UNKNOWN_ERROR.getCode());
            response.setResult(Result.UNKNOWN_ERROR);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/new")
    public ResponseEntity<CreateAccountResponse> createAccount(@RequestBody CreateAccountRequest request)
    {
        CreateAccountResponse response = new CreateAccountResponse();
        try
        {
            AccountRecord record = service.createAccount(request.getSymbol(), request.getLabel(), request.getEnabled());

            response.setAddress(record.getAddress());
            response.setCreatedDate(record.getCreatedDate());
            response.setEnabled(record.isEnabled());
            response.setId(record.getId());
            response.setLabel(record.getLabel());

            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (InvalidCoinSymbolException e)
        {
            response.setResultCode(Result.INVALID_COIN_SYMBOL.getCode());
            response.setResult(Result.INVALID_COIN_SYMBOL);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        catch (CryptoException e)
        {
            response.setResultCode(Result.INVALID_CRYPTO_OPERATION.getCode());
            response.setResult(Result.INVALID_CRYPTO_OPERATION);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        catch (Throwable t)
        {
            logger.error("failed to create new enterprise account.", t);
            response.setResultCode(Result.UNKNOWN_ERROR.getCode());
            response.setResult(Result.UNKNOWN_ERROR);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{walletId}/address")
    public ResponseEntity<GetAddressResponse> getAccountAddress(@PathVariable String walletId)
    {
        GetAddressResponse response = new GetAddressResponse();
        try
        {
            String address = this.service.getAccountAddress(walletId);
            response.setAddress(address);

            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (AccountNotFoundException e)
        {
            response.setResultCode(Result.ACCOUNT_NOT_FOUND.getCode());
            response.setResult(Result.ACCOUNT_NOT_FOUND);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        catch (Throwable e)
        {
            response.setResultCode(Result.UNKNOWN_ERROR.getCode());
            response.setResult(Result.UNKNOWN_ERROR);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{walletId}/balances")
    public ResponseEntity<GetAllTokenBalancesResponse> getAccountBalance(@PathVariable String walletId)
    {
        GetAllTokenBalancesResponse response = new GetAllTokenBalancesResponse();
        try
        {
            Map<String, BigInteger> balances = this.service.getBalances(walletId);

            for (Map.Entry<String, BigInteger> entry : balances.entrySet())
            {
                logger.debug("add {} to response. value = {}", entry.getKey(), entry.getValue());
                response.addBalance(entry.getKey(), entry.getValue());
            }

            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (AccountNotFoundException e)
        {
            response.setResultCode(Result.ACCOUNT_NOT_FOUND.getCode());
            response.setResult(Result.ACCOUNT_NOT_FOUND);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        catch (Throwable t)
        {
            logger.error("failed to get enterprise account balance.", t);
            response.setResultCode(Result.UNKNOWN_ERROR.getCode());
            response.setResult(Result.UNKNOWN_ERROR);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{walletId}/send")
    public ResponseEntity<SendCoinResponse> send(@PathVariable String walletId, @RequestBody SendCoinRequest request)
    {
        SendCoinResponse response = new SendCoinResponse();

        try
        {
            String id = service.send(request.getSymbol(), walletId, request.getAddress(), request.getAmount(), request.getInternalTransactionId());

            response.setTransactionId(id);

            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (InvalidCoinSymbolException e)
        {
            response.setResultCode(Result.INVALID_COIN_SYMBOL.getCode());
            response.setResult(Result.INVALID_COIN_SYMBOL);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        catch (Throwable e)
        {
            logger.error("failed to send coin.", e);
            response.setResultCode(Result.UNKNOWN_ERROR.getCode());
            response.setResult(Result.UNKNOWN_ERROR);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
