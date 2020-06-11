package io.acrosafe.wallet.hot.eth.web.rest.response;

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Balance
{
    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("balance")
    private BigInteger balance;

    public Balance(String symbol, BigInteger balance)
    {
        this.symbol = symbol;
        this.balance = balance;
    }

    public String getSymbol()
    {
        return symbol;
    }

    public void setSymbol(String symbol)
    {
        this.symbol = symbol;
    }

    public BigInteger getBalance()
    {
        return balance;
    }

    public void setBalance(BigInteger balance)
    {
        this.balance = balance;
    }
}
