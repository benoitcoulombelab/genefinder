/*
 * Copyright (c) 2014 Institut de recherches cliniques de Montreal (IRCM)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ca.qc.ircm.genefinder.test.config;

import java.lang.reflect.Method;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.opentest4j.TestAbortedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allow test to be tried again on fail.
 */
public class RetryOnFailExtension implements InvocationInterceptor {
  private static final Logger logger = LoggerFactory.getLogger(RetryOnFailExtension.class);

  @Override
  public void interceptTestMethod(Invocation<Void> invocation,
      ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext)
      throws Throwable {
    RetryOnFail retryOnFail = null;
    retryOnFail = extensionContext.getTestMethod().map(me -> me.getAnnotation(RetryOnFail.class))
        .orElseGet(() -> extensionContext.getTestClass()
            .map(cl -> cl.getAnnotation(RetryOnFail.class)).orElse(null));
    if (retryOnFail != null) {
      int retryCount = 0;
      Throwable lastException = null;

      while (!(lastException instanceof TestAbortedException)
          && retryCount <= retryOnFail.value()) {
        try {
          if (retryCount > 0) {
            logger.warn("retry test {} that failed, try {} out of {}",
                extensionContext.getTestMethod().map(Method::getName).orElse(""), retryCount,
                retryOnFail.value(), lastException);
          }
          logger.debug("running test {}",
              extensionContext.getTestMethod().map(Method::getName).orElse(""));
          invocation.proceed();
          return;
        } catch (Throwable exception) {
          lastException = exception;
          retryCount++;
        }
      }
      throw lastException;
    } else {
      logger.debug("running test {}",
          extensionContext.getTestMethod().map(Method::getName).orElse(""));
      invocation.proceed();
    }
  }
}