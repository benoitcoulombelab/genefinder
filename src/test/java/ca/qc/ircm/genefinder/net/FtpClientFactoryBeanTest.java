package ca.qc.ircm.genefinder.net;

import static org.junit.Assert.assertTrue;

import ca.qc.ircm.genefinder.test.config.Rules;
import org.apache.commons.net.ftp.FTPClient;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class FtpClientFactoryBeanTest {
  private FtpClientFactoryBean ftpClientFactoryBean = new FtpClientFactoryBean();
  @Rule
  public RuleChain rules = Rules.defaultRules(this);

  @Test
  public void create() {
    FTPClient ftpClient = ftpClientFactoryBean.create();

    assertTrue(ftpClient instanceof FTPClient);
  }
}
