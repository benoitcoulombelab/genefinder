package ca.qc.ircm.genefinder.test.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allow test to be tried again on fail.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface RetryOnFail {
  /**
   * Returns number of retries to do before test fails.
   * 
   * @return number of retries to do before test fails
   */
  public int value();
}
