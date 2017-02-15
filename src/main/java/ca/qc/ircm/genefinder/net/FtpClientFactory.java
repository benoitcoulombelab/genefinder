package ca.qc.ircm.genefinder.net;

import org.apache.commons.net.ftp.FTPClient;
import org.springframework.stereotype.Component;

/**
 * Creates instances of {@link FTPClient}.
 */
@Component
public class FtpClientFactory {
  public FTPClient create() {
    return new FTPClient();
  }
}
