package ca.qc.ircm.genefinder.data;

import static org.mockito.Mockito.verify;

import ca.qc.ircm.genefinder.annotation.ProteinMapping;
import ca.qc.ircm.genefinder.test.config.ServiceTestAnnotations;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class GuessDataWriterTest {
  private GuessDataWriter guessDataWriter;
  @Mock
  private ExcelDataWriter excelDataWriter;
  @Mock
  private TextDataWriter textDataWriter;
  @Mock
  private File output;
  @Mock
  private FindGenesParameters parameters;
  @Mock
  private Map<String, ProteinMapping> mappings;

  @Before
  public void beforeTest() {
    guessDataWriter = new GuessDataWriter(excelDataWriter, textDataWriter);
  }

  @Test
  public void writeGene_Text() throws Throwable {
    File input = new File("data.txt");

    guessDataWriter.writeGene(input, output, parameters, mappings);

    verify(textDataWriter).writeGene(input, output, parameters, mappings);
  }

  @Test
  public void writeGene_Excel2003() throws Throwable {
    File input = new File("data.xls");

    guessDataWriter.writeGene(input, output, parameters, mappings);

    verify(excelDataWriter).writeGene(input, output, parameters, mappings);
  }

  @Test
  public void writeGene_Excel2007() throws Throwable {
    File input = new File("data.xlsx");

    guessDataWriter.writeGene(input, output, parameters, mappings);

    verify(excelDataWriter).writeGene(input, output, parameters, mappings);
  }

  @Test
  public void writeGene_ExcelMacro() throws Throwable {
    File input = new File("data.xlsm");

    guessDataWriter.writeGene(input, output, parameters, mappings);

    verify(excelDataWriter).writeGene(input, output, parameters, mappings);
  }
}
