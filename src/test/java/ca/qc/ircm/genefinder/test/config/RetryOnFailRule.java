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

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allow test to be tried again on fail.
 */
public class RetryOnFailRule implements TestRule {
  private final Logger logger = LoggerFactory.getLogger(RetryOnFailRule.class);

  @Override
  public Statement apply(final Statement base, final Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        RetryOnFail retryOnFail = null;
        if (description.getAnnotation(RetryOnFail.class) != null) {
          retryOnFail = description.getAnnotation(RetryOnFail.class);
        } else if (description.getTestClass().getAnnotation(RetryOnFail.class) != null) {
          retryOnFail = description.getTestClass().getAnnotation(RetryOnFail.class);
        }
        if (retryOnFail != null) {
          int retryCount = 0;
          Throwable lastException = null;

          while (retryCount <= retryOnFail.value()) {
            try {
              if (retryCount > 0) {
                logger.warn("retry test {} that failed, try {} out of {}",
                    description.getMethodName(), retryCount, retryOnFail.value(), lastException);
              }
              base.evaluate();
              return;
            } catch (Throwable e) {
              lastException = e;
              retryCount++;
            }
          }
          throw lastException;
        } else {
          base.evaluate();
        }
      }
    };
  }
}