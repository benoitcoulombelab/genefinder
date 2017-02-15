package ca.qc.ircm.genefinder.net;

import org.apache.commons.net.ftp.FTPClient;
import org.springframework.stereotype.Component;

/**
 * Creates instances of {@link FTPClient}.
 */
@Component
public class FtpClientFactoryBean implements FtpClientFactory {
  @Override
  public FTPClient create() {
    return new FTPClient();
  }
}
