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
    assertEquals("ftp.ncbi.nlm.nih.gov", ncbiConfiguration.ftp());
    assertEquals("/pub/taxonomy/taxdmp.zip", ncbiConfiguration.taxonomy());
    assertEquals("nodes.dmp", ncbiConfiguration.taxonomyNodes());
    assertEquals("/gene/DATA/gene2refseq.gz", ncbiConfiguration.gene2accession());
    assertEquals("/gene/DATA/gene_info.gz", ncbiConfiguration.geneInfo());
    assertEquals(Pattern.compile("^(?:ref\\|)?([ANYXZ]P_\\d+\\.\\d+)").pattern(),
        ncbiConfiguration.refseqProteinAccessionPattern().pattern());
    assertEquals(Pattern.compile("^(?:gi\\|)?(\\d+)").pattern(),
        ncbiConfiguration.refseqProteinGiPattern().pattern());
    assertEquals("/refseq/release/complete", ncbiConfiguration.refseqSequences());
    assertEquals(Pattern.compile(".+\\.protein\\.faa\\.gz").pattern(),
        ncbiConfiguration.refseqSequencesFilenamePattern().pattern());
    assertEquals("http://eutils.ncbi.nlm.nih.gov/entrez/eutils", ncbiConfiguration.eutils());
    assertEquals(1000, ncbiConfiguration.maxIdsPerRequest());
  }
}
