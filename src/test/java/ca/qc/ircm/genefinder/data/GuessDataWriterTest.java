/*
 * Copyright (c) 2014 Institut de recherches cliniques de Montreal (IRCM)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

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
