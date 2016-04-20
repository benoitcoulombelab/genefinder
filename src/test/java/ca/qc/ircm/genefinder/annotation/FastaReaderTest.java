package ca.qc.ircm.genefinder.annotation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import ca.qc.ircm.genefinder.test.config.Rules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FastaReaderTest {
  @Rule
  public RuleChain rules = Rules.defaultRules(this);

  @Test
  public void parse() throws Throwable {
    Path fasta =
        Paths.get(getClass().getResource("/annotation/UP000005640_9606_small.fasta").toURI());
    try (FastaReader reader =
        new FastaReader(Files.newBufferedReader(fasta, Charset.forName("UTF-8")))) {
      Sequence sequence = reader.nextSequence();
      assertEquals(
          "tr|A0A024R161|A0A024R161_HUMAN Guanine nucleotide-binding protein subunit gamma "
              + "OS=Homo sapiens GN=DNAJC25-GNG10 PE=3 SV=1",
          sequence.getName());
      assertEquals("MGAPLLSPGWGAGAAGRRWWMLLAPLLPALLLVRPAGALVEGLYCGTRDCYEVLGVSRSA"
          + "GKAEIARAYRQLARRYHPDRYRPQPGDEGPGRTPQSAEEAFLLVATAYETLKVSQAAAEL"
          + "QQYCMQNACKDALLVGVPAGSNPFREPRSCALL", sequence.getSequence());
      sequence = reader.nextSequence();
      assertEquals("tr|A0A024RAP8|A0A024RAP8_HUMAN HCG2009644, isoform CRA_b OS=Homo sapiens "
          + "GN=KLRC4-KLRK1 PE=4 SV=1", sequence.getName());
      assertEquals("MGWIRGRRSRHSWEMSEFHNYNLDLKKSDFSTRWQKQRCPVVKSKCRENASPFFFCCFIA"
          + "VAMGIRFIIMVTIWSAVFLNSLFNQEVQIPLTESYCGPCPKNWICYKNNCYQFFDESKNW"
          + "YESQASCMSQNASLLKVYSKEDQDLLKLVKSYHWMGLVHIPTNGSWQWEDGSILSPNLLT"
          + "IIEMQKGDCALYASSFKGYIENCSTPNTYICMQRTV", sequence.getSequence());
      sequence = reader.nextSequence();
      assertEquals("tr|X6R8D5|X6R8D5_HUMAN Uncharacterized protein OS=Homo sapiens PE=4 SV=2",
          sequence.getName());
      assertEquals(
          "MGRKEHESPSQPHMCGWEDSQKPSVPSHGPKTPSCKGVKAPHSSRPRAWKQDLEQSLAAA"
              + "YVPVVVDSKGQNPDKLRFNFYTSQYSNSLNPFYTLQKPTCGYLYRRDTDHTRKRFDVPPA" + "NLVLWRS",
          sequence.getSequence());
      sequence = reader.nextSequence();
      assertNull(sequence);
    }
  }

  @Test
  public void parse_Empty() throws Throwable {
    try (FastaReader reader = new FastaReader(new BufferedReader(new StringReader("")))) {
      assertNull(reader.nextSequence());
    }
  }
}
