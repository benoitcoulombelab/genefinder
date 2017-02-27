package ca.qc.ircm.genefinder.data;

import static org.mockito.Mockito.verify;

import ca.qc.ircm.genefinder.test.config.ServiceTestAnnotations;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class GuessProteinParserTest {
  private GuessProteinParser guessProteinParser;
  @Mock
  private ExcelProteinParser excelProteinParser;
  @Mock
  private TextProteinParser textProteinParser;
  @Mock
  private FindGenesParameters parameters;

  @Before
  public void beforeTest() {
    guessProteinParser = new GuessProteinParser(excelProteinParser, textProteinParser);
  }

  @Test
  public void parseProteinIds_Text() throws Throwable {
    File input = new File("data.txt");

    guessProteinParser.parseProteinIds(input, parameters);

    verify(textProteinParser).parseProteinIds(input, parameters);
  }

  @Test
  public void parseProteinIds_Excel2003() throws Throwable {
    File input = new File("data.xls");

    guessProteinParser.parseProteinIds(input, parameters);

    verify(excelProteinParser).parseProteinIds(input, parameters);
  }

  @Test
  public void parseProteinIds_Excel2007() throws Throwable {
    File input = new File("data.xlsx");

    guessProteinParser.parseProteinIds(input, parameters);

    verify(excelProteinParser).parseProteinIds(input, parameters);
  }

  @Test
  public void parseProteinIds_ExcelMacro() throws Throwable {
    File input = new File("data.xlsm");

    guessProteinParser.parseProteinIds(input, parameters);

    verify(excelProteinParser).parseProteinIds(input, parameters);
  }
}
