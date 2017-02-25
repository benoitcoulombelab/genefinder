package ca.qc.ircm.genefinder.rest;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = RestConfigurationSpringBoot.PREFIX)
public class RestConfigurationSpringBoot implements RestConfiguration {
  public static final String PREFIX = "rest";
  private int timeout;

  @Override
  public int timeout() {
    return timeout;
  }

  public int getTimeout() {
    return timeout;
  }

  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }
}
