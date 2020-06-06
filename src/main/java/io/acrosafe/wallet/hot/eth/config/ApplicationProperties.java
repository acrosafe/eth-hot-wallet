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
package io.acrosafe.wallet.hot.eth.config;

import io.acrosafe.wallet.core.eth.Passphrase;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties
{
    private String serviceId;
    private Passphrase passphrase;
    private Boolean testnet = true;
    private String serviceUrl;
    private Long serviceTimeout = 600L;
    private int entropyBits = 256;
    private int securityStrength = 256;

    public Long getServiceTimeout()
    {
        return serviceTimeout;
    }

    public void setServiceTimeout(Long serviceTimeout)
    {
        this.serviceTimeout = serviceTimeout;
    }

    public Boolean getTestnet()
    {
        return testnet;
    }

    public void setTestnet(Boolean testnet)
    {
        this.testnet = testnet;
    }

    public String getServiceUrl()
    {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl)
    {
        this.serviceUrl = serviceUrl;
    }

    public Passphrase getPassphrase()
    {
        return passphrase;
    }

    public void setPassphrase(String passphrase)
    {
        this.passphrase = new Passphrase(passphrase);
    }

    public String getServiceId()
    {
        return serviceId;
    }

    public void setServiceId(String serviceId)
    {
        this.serviceId = serviceId;
    }

    public int getEntropyBits()
    {
        return entropyBits;
    }

    public void setEntropyBits(int entropyBits)
    {
        this.entropyBits = entropyBits;
    }

    public int getSecurityStrength()
    {
        return securityStrength;
    }

    public void setSecurityStrength(int securityStrength)
    {
        this.securityStrength = securityStrength;
    }
}
