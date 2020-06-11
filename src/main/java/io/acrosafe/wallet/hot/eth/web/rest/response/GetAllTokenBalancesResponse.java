package io.acrosafe.wallet.hot.eth.web.rest.response;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetAllTokenBalancesResponse extends Response
{
    @JsonProperty("balances")
    private List<Balance> balanceList;

    public List<Balance> getBalanceList()
    {
        return balanceList;
    }

    public void setBalanceList(List<Balance> balanceList)
    {
        this.balanceList = balanceList;
    }

    public void addBalance(String symbol, BigInteger balance)
    {
        if (balanceList == null)
        {
            balanceList = new ArrayList<>();
        }

        balanceList.add(new Balance(symbol, balance));
    }
}
