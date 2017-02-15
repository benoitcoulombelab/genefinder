package ca.qc.ircm.genefinder.net;

import static org.junit.Assert.assertTrue;

import ca.qc.ircm.genefinder.test.config.ServiceTestAnnotations;
import org.apache.commons.net.ftp.FTPClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class FtpClientFactoryBeanTest {
  private FtpClientFactoryBean ftpClientFactoryBean = new FtpClientFactoryBean();

  @Test
  public void create() {
    FTPClient ftpClient = ftpClientFactoryBean.create();

    assertTrue(ftpClient instanceof FTPClient);
  }
}
