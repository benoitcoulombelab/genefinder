package ca.qc.ircm.genefinder.net;

import org.apache.commons.net.ftp.FTPClient;

/**
 * Creates instances of {@link FTPClient}.
 */
public class FtpClientFactoryBean implements FtpClientFactory {
  @Override
  public FTPClient create() {
    return new FTPClient();
  }
}
