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
package io.acrosafe.wallet.hot.eth.web.rest.response;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateAccountResponse extends Response
{
    @JsonProperty("id")
    private String id;

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("label")
    private String label;

    @JsonProperty("enabled")
    private boolean enabled;

    @JsonProperty("address")
    private String address;

    @JsonProperty("created_date")
    private Instant createdDate;

    /**
     * Returns the wallet id.
     * 
     * @return
     */
    public String getId()
    {
        return id;
    }

    /**
     * Sets the wallet id.
     * 
     * @param id
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * Returns the coin symbol.
     * 
     * @return
     */
    public String getSymbol()
    {
        return symbol;
    }

    /**
     * Sets the coin symbol.
     * 
     * @param symbol
     */
    public void setSymbol(String symbol)
    {
        this.symbol = symbol;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public Instant getCreatedDate()
    {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate)
    {
        this.createdDate = createdDate;
    }
}
