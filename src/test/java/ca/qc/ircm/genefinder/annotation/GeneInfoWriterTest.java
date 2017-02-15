package ca.qc.ircm.genefinder.annotation;

import static org.junit.Assert.assertEquals;

import ca.qc.ircm.genefinder.test.config.ServiceTestAnnotations;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class GeneInfoWriterTest {
  private GeneInfoWriter geneInfoWriter;
  private StringWriter stringWriter = new StringWriter();

  @Before
  public void beforeTest() {
    geneInfoWriter = new GeneInfoWriter(stringWriter);
  }

  private Date toDate(LocalDate localDate) {
    return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
  }

  private List<GeneInfo> createGeneInfos() {
    final List<GeneInfo> geneInfos = new ArrayList<>();
    GeneInfo geneInfo = new GeneInfo();
    geneInfo.setOrganismId(9606);
    geneInfo.setId(1);
    geneInfo.setSymbol("A1BG");
    geneInfo.setLocusTag(null);
    geneInfo.setSynonyms(new ArrayList<>());
    geneInfo.getSynonyms().add("A1B");
    geneInfo.getSynonyms().add("ABG");
    geneInfo.getSynonyms().add("GAB");
    geneInfo.getSynonyms().add("HYST2477");
    geneInfo.setDbXrefs(new ArrayList<>());
    geneInfo.getDbXrefs().add("MIM:138670");
    geneInfo.getDbXrefs().add("HGNC:HGNC:5");
    geneInfo.getDbXrefs().add("Ensembl:ENSG00000121410");
    geneInfo.getDbXrefs().add("HPRD:00726");
    geneInfo.getDbXrefs().add("Vega:OTTHUMG00000183507");
    geneInfo.setChromosome("19");
    geneInfo.setMapLocation("19q13.4");
    geneInfo.setDescription("alpha-1-B glycoprotein");
    geneInfo.setTypeOfGene("protein-coding");
    geneInfo.setSymbolFromNomenclatureAuthority("A1BG");
    geneInfo.setFullNameFromNomenclatureAuthority("alpha-1-B glycoprotein");
    geneInfo.setNomenclatureStatus("O");
    geneInfo.setOtherDesignations(new ArrayList<>());
    geneInfo.getOtherDesignations().add("HEL-S-163pA");
    geneInfo.getOtherDesignations().add("epididymis secretory sperm binding protein Li 163pA");
    geneInfo.setModificationDate(toDate(LocalDate.of(2016, 2, 7)));
    geneInfos.add(geneInfo);
    geneInfo = new GeneInfo();
    geneInfo.setOrganismId(9606);
    geneInfo.setId(2149);
    geneInfo.setSymbol("F2R");
    geneInfo.setLocusTag(null);
    geneInfo.setSynonyms(new ArrayList<>());
    geneInfo.getSynonyms().add("CF2R");
    geneInfo.getSynonyms().add("HTR");
    geneInfo.getSynonyms().add("PAR-1");
    geneInfo.getSynonyms().add("PAR1");
    geneInfo.getSynonyms().add("TR");
    geneInfo.setDbXrefs(new ArrayList<>());
    geneInfo.getDbXrefs().add("MIM:187930");
    geneInfo.getDbXrefs().add("HGNC:HGNC:3537");
    geneInfo.getDbXrefs().add("Ensembl:ENSG00000181104");
    geneInfo.getDbXrefs().add("HPRD:01763");
    geneInfo.getDbXrefs().add("Vega:OTTHUMG00000131299");
    geneInfo.setChromosome("5");
    geneInfo.setMapLocation("5q13");
    geneInfo.setDescription("coagulation factor II thrombin receptor");
    geneInfo.setTypeOfGene("protein-coding");
    geneInfo.setSymbolFromNomenclatureAuthority("F2R");
    geneInfo.setFullNameFromNomenclatureAuthority("coagulation factor II thrombin receptor");
    geneInfo.setNomenclatureStatus("O");
    geneInfo.setOtherDesignations(new ArrayList<>());
    geneInfo.getOtherDesignations().add("coagulation factor II (thrombin) receptor");
    geneInfo.getOtherDesignations().add("protease-activated receptor 1");
    geneInfo.setModificationDate(toDate(LocalDate.of(2016, 2, 7)));
    geneInfos.add(geneInfo);
    geneInfo = new GeneInfo();
    geneInfo.setOrganismId(9606);
    geneInfo.setId(4404);
    geneInfo.setSymbol("MRX39");
    geneInfo.setLocusTag(null);
    geneInfo.setSynonyms(new ArrayList<>());
    geneInfo.setDbXrefs(new ArrayList<>());
    geneInfo.getDbXrefs().add("HGNC:HGNC:7270");
    geneInfo.setChromosome("X");
    geneInfo.setMapLocation(null);
    geneInfo.setDescription("mental retardation, X-linked 39");
    geneInfo.setTypeOfGene("unknown");
    geneInfo.setSymbolFromNomenclatureAuthority("MRX39");
    geneInfo.setFullNameFromNomenclatureAuthority("mental retardation, X-linked 39");
    geneInfo.setNomenclatureStatus("O");
    geneInfo.setOtherDesignations(new ArrayList<>());
    geneInfo.setModificationDate(toDate(LocalDate.of(2014, 12, 7)));
    geneInfos.add(geneInfo);
    geneInfo = new GeneInfo();
    geneInfo.setOrganismId(741158);
    geneInfo.setId(8923219);
    geneInfo.setSymbol("16S rRNA");
    geneInfo.setLocusTag(null);
    geneInfo.setSynonyms(new ArrayList<>());
    geneInfo.setDbXrefs(new ArrayList<>());
    geneInfo.setChromosome("MT");
    geneInfo.setMapLocation(null);
    geneInfo.setDescription("l-rRNA");
    geneInfo.setTypeOfGene("rRNA");
    geneInfo.setSymbolFromNomenclatureAuthority(null);
    geneInfo.setFullNameFromNomenclatureAuthority(null);
    geneInfo.setNomenclatureStatus(null);
    geneInfo.setOtherDesignations(new ArrayList<>());
    geneInfo.setModificationDate(toDate(LocalDate.of(2012, 11, 27)));
    geneInfos.add(geneInfo);
    return geneInfos;
  }

  @Test
  public void writeHeader() throws Throwable {
    Path file =
        Paths.get(getClass().getResource("/annotation/Homo_sapiens_small.gene_info").toURI());
    List<String> expectedLines = Files.readAllLines(file);

    geneInfoWriter.writeHeader();

    String header = stringWriter.toString();
    header = header.substring(0, header.length() - 1);
    assertEquals(expectedLines.get(0), header);
  }

  @Test
  public void writeGeneInfo() throws Throwable {
    Path file =
        Paths.get(getClass().getResource("/annotation/Homo_sapiens_small.gene_info").toURI());
    List<String> expectedLines = Files.readAllLines(file);
    List<GeneInfo> geneInfos = createGeneInfos();

    for (GeneInfo geneInfo : geneInfos) {
      geneInfoWriter.writeGeneInfo(geneInfo);
    }

    String[] lines = stringWriter.toString().split("\n");
    for (int i = 0; i < lines.length; i++) {
      assertEquals(expectedLines.get(i + 1), lines[i]);
    }
  }
}
