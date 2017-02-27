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
public class UniprotConfigurationTest {
  @Inject
  private UniprotConfiguration uniprotConfiguration;

  @Test
  public void defaultProperties() throws Throwable {
    assertEquals("ftp.uniprot.org", uniprotConfiguration.ftp());
    assertEquals("http://www.uniprot.org/uploadlists", uniprotConfiguration.mapping());
    assertEquals(Pattern
        .compile("^(?:\\w{2}\\|)?([OPQ][0-9][A-Z0-9]{3}[0-9])(?:-\\d+)?(?:\\|.*)?"
            + "|^(?:\\w{2}\\|)?([A-NR-Z][0-9]([A-Z][A-Z0-9]{2}[0-9]){1,2})(?:-\\d+)?(?:\\|.*)?")
        .pattern(), uniprotConfiguration.proteinIdPattern().pattern());
    assertEquals(100, uniprotConfiguration.maxIdsPerRequest());
  }
}
