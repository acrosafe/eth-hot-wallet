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

import io.acrosafe.wallet.core.eth.BlockChainNetwork;
import io.acrosafe.wallet.core.eth.SeedGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class BlockChainConfiguration
{
    private static final Long DEFAULT_SERVICE_TIMEOUT = 600L;

    private final Environment env;

    public BlockChainConfiguration(Environment env)
    {
        this.env = env;
    }

    @Bean
    public SeedGenerator seedGenerator()
    {
        return new SeedGenerator();
    }

    @Bean
    public BlockChainNetwork blockChainNetwork()
    {
        String serviceUrl = env.getProperty("application.service-url");
        Long serviceTimeout = env.getProperty("application.service-timeout", Long.class, DEFAULT_SERVICE_TIMEOUT);
        BlockChainNetwork blockChainNetwork = new BlockChainNetwork(serviceUrl, serviceTimeout);
        return blockChainNetwork;
    }
}
