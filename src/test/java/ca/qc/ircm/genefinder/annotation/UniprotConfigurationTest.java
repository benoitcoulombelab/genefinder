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
    assertEquals("http://www.uniprot.org/uniprot", uniprotConfiguration.search());
    assertEquals("/pub/databases/uniprot/current_release/knowledgebase/idmapping/idmapping.dat.gz",
        uniprotConfiguration.idmapping());
    assertEquals(
        "/pub/databases/uniprot/current_release/knowledgebase/complete/uniprot_sprot.fasta.gz",
        uniprotConfiguration.swissprotFasta());
    assertEquals(
        "/pub/databases/uniprot/current_release/knowledgebase/complete/uniprot_trembl.fasta.gz",
        uniprotConfiguration.tremblFasta());
    assertEquals("/pub/databases/uniprot/current_release/knowledgebase/reference_proteomes",
        uniprotConfiguration.referenceProteomes());
    assertEquals(Pattern.compile("UP\\d+_(\\d+)[\\._].+").pattern(),
        uniprotConfiguration.filenamePattern().pattern());
    assertEquals("GI", uniprotConfiguration.giMapping());
    assertEquals("RefSeq", uniprotConfiguration.refseqMapping());
    assertEquals("NCBI_TaxID", uniprotConfiguration.taxonMapping());
    assertEquals("GeneID", uniprotConfiguration.geneMapping());
    assertEquals(Pattern
        .compile("^(?:\\w{2}\\|)?([OPQ][0-9][A-Z0-9]{3}[0-9])(?:-\\d+)?(?:\\|.*)?"
            + "|^(?:\\w{2}\\|)?([A-NR-Z][0-9]([A-Z][A-Z0-9]{2}[0-9]){1,2})(?:-\\d+)?(?:\\|.*)?")
        .pattern(), uniprotConfiguration.proteinIdPattern().pattern());
  }
}
