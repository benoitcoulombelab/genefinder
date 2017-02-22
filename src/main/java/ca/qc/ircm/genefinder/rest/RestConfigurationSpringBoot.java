package ca.qc.ircm.genefinder.rest;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = RestConfigurationSpringBoot.PREFIX)
public class RestConfigurationSpringBoot implements RestConfiguration {
  public static final String PREFIX = "rest";
  private long timeout;

  @Override
  public long timeout() {
    return timeout;
  }

  public long getTimeout() {
    return timeout;
  }

  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }
}
