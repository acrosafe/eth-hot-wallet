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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.task.AsyncTaskExecutor;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class ExceptionHandlingAsyncTaskExecutor implements AsyncTaskExecutor, InitializingBean, DisposableBean
{
    private static final Logger log = LoggerFactory.getLogger(ExceptionHandlingAsyncTaskExecutor.class);
    private static final String EXCEPTION_MESSAGE = "caught async exception";

    private final AsyncTaskExecutor executor;

    public ExceptionHandlingAsyncTaskExecutor(AsyncTaskExecutor executor)
    {
        this.executor = executor;
    }

    @Override
    public void execute(Runnable task)
    {
        executor.execute(createWrappedRunnable(task));
    }

    @Override
    public void execute(Runnable task, long startTimeout)
    {
        executor.execute(createWrappedRunnable(task), startTimeout);
    }

    private <T> Callable<T> createCallable(final Callable<T> task)
    {
        return () -> {
            try
            {
                return task.call();
            }
            catch (Exception e)
            {
                handle(e);
                throw e;
            }
        };
    }

    private Runnable createWrappedRunnable(final Runnable task)
    {
        return () -> {
            try
            {
                task.run();
            }
            catch (Exception e)
            {
                handle(e);
            }
        };
    }

    private void handle(Exception e)
    {
        log.error(EXCEPTION_MESSAGE, e);
    }

    @Override
    public Future<?> submit(Runnable task)
    {
        return executor.submit(createWrappedRunnable(task));
    }

    @Override
    public <T> Future<T> submit(Callable<T> task)
    {
        return executor.submit(createCallable(task));
    }

    @Override
    public void destroy() throws Exception
    {
        if (executor instanceof DisposableBean)
        {
            DisposableBean bean = (DisposableBean) executor;
            bean.destroy();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        if (executor instanceof InitializingBean)
        {
            InitializingBean bean = (InitializingBean) executor;
            bean.afterPropertiesSet();
        }
    }
}
