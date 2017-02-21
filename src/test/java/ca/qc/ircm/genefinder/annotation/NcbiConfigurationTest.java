package ca.qc.ircm.genefinder.annotation;

import static org.junit.Assert.assertEquals;

import ca.qc.ircm.genefinder.test.config.ServiceTestAnnotations;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class NcbiConfigurationTest {
  @Inject
  private NcbiConfiguration ncbiConfiguration;

  @Test
  public void defaultProperties() throws Throwable {
    assertEquals("ftp.ncbi.nlm.nih.gov", ncbiConfiguration.ftp());
    assertEquals("/gene/DATA/gene_info.gz", ncbiConfiguration.geneInfo());
  }
}
