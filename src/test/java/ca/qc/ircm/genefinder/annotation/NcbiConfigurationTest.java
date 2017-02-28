package ca.qc.ircm.genefinder.annotation;

import static org.junit.Assert.assertEquals;

import ca.qc.ircm.genefinder.test.config.ServiceTestAnnotations;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.regex.Pattern;

import javax.inject.Inject;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class NcbiConfigurationTest {
  @Inject
  private NcbiConfiguration ncbiConfiguration;

  @Test
  public void defaultProperties() throws Throwable {
    assertEquals(Pattern.compile("^(?:ref\\|)?([ANYXZ]P_\\d+\\.\\d+)").pattern(),
        ncbiConfiguration.refseqProteinAccessionPattern().pattern());
    assertEquals(Pattern.compile("^(?:gi\\|)?(\\d+)").pattern(),
        ncbiConfiguration.refseqProteinGiPattern().pattern());
    assertEquals("https://eutils.ncbi.nlm.nih.gov/entrez/eutils", ncbiConfiguration.eutils());
    assertEquals(1000, ncbiConfiguration.maxIdsPerRequest());
  }
}
