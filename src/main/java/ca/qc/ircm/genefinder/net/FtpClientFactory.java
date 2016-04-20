package ca.qc.ircm.genefinder.net;

import org.apache.commons.net.ftp.FTPClient;

/**
 * Creates instances of {@link FTPClient}.
 */
public interface FtpClientFactory {
  public FTPClient create();
}
