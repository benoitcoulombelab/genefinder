package ca.qc.ircm.genefinder.data.gui;

import static ca.qc.ircm.genefinder.annotation.ProteinDatabase.REFSEQ;
import static ca.qc.ircm.genefinder.annotation.ProteinDatabase.REFSEQ_GI;
import static ca.qc.ircm.genefinder.annotation.ProteinDatabase.UNIPROT;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Locale;

public class ProteinDatabaseStringConverterTest {
  private Locale locale = Locale.getDefault();
  private ProteinDatabaseStringConverter proteinDatabaseStringConverter =
      new ProteinDatabaseStringConverter(locale);

  @Test
  public void fromString_Refseq() {
    assertEquals(REFSEQ, proteinDatabaseStringConverter.fromString(REFSEQ.getLabel(locale)));
  }

  @Test
  public void fromString_RefseqGi() {
    assertEquals(REFSEQ_GI, proteinDatabaseStringConverter.fromString(REFSEQ_GI.getLabel(locale)));
  }

  @Test
  public void fromString_Uniprot() {
    assertEquals(UNIPROT, proteinDatabaseStringConverter.fromString(UNIPROT.getLabel(locale)));
  }

  @Test
  public void toString_Refseq() {
    assertEquals(REFSEQ.getLabel(locale), proteinDatabaseStringConverter.toString(REFSEQ));
  }

  @Test
  public void toString_RefseqGi() {
    assertEquals(REFSEQ_GI.getLabel(locale), proteinDatabaseStringConverter.toString(REFSEQ_GI));
  }

  @Test
  public void toString_Uniprot() {
    assertEquals(UNIPROT.getLabel(locale), proteinDatabaseStringConverter.toString(UNIPROT));
  }
}
