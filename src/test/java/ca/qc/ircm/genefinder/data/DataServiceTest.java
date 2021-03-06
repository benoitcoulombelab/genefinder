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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.genefinder.annotation.DownloadProteinMappingService;
import ca.qc.ircm.genefinder.annotation.GeneInfo;
import ca.qc.ircm.genefinder.annotation.ProteinMapping;
import ca.qc.ircm.genefinder.test.config.ServiceTestAnnotations;
import ca.qc.ircm.progressbar.ProgressBar;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

@ServiceTestAnnotations
public class DataServiceTest {
  private DataService dataServiceBean;
  @Mock
  private DownloadProteinMappingService proteinMappingService;
  @Mock
  private ProteinParser proteinParser;
  @Mock
  private DataWriter dataWriter;
  @Mock
  private ProgressBar progressBar;
  @Mock
  private List<String> proteinIds;
  @Captor
  private ArgumentCaptor<Map<String, ProteinMapping>> mappingsCaptor;
  @TempDir
  File temporaryFolder;
  private Locale locale;

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    dataServiceBean = new DataService(proteinMappingService, proteinParser, dataWriter);
    locale = Locale.getDefault();
    when(progressBar.step(any(Double.class))).thenReturn(progressBar);
  }

  @Test
  public void findGeneNames() throws Throwable {
    File file = new File(getClass().getResource("/proteinGroups.txt").toURI());
    File input = new File(temporaryFolder, "proteinGroups.txt");
    FileUtils.copyFile(file, input);
    final File output = new File(temporaryFolder, "proteinGroupsWithGene.txt");
    final List<File> files = Arrays.asList(input);
    List<ProteinMapping> mappings = new ArrayList<>();
    mappings.add(getProteinMapping("4262120", "ABC"));
    mappings.add(getProteinMapping("58201131", "ABC"));
    mappings.add(getProteinMapping("270297794", "ABC"));
    mappings.add(getProteinMapping("13560677", "FFE"));
    mappings.add(getProteinMapping("13492060", "RTS"));
    mappings.add(getProteinMapping("63100331", "FAF"));
    mappings.add(getProteinMapping("30583211", "FAF"));
    mappings.add(getProteinMapping("17512236", "FAF"));
    when(proteinParser.parseProteinIds(any(), any())).thenReturn(proteinIds);
    when(proteinMappingService.downloadProteinMappings(any(), any(), any(), any()))
        .thenReturn(mappings);
    FindGenesParametersBean parameters = new FindGenesParametersBean();

    dataServiceBean.findGeneNames(files, parameters, progressBar, locale);

    verify(progressBar, atLeastOnce()).setProgress(any(Double.class));
    verify(progressBar, atLeastOnce()).setMessage(any(String.class));
    verify(proteinParser).parseProteinIds(eq(input), eq(parameters));
    verify(proteinMappingService).downloadProteinMappings(eq(proteinIds), eq(parameters),
        eq(progressBar), eq(locale));
    verify(dataWriter).writeGene(eq(input), eq(output), eq(parameters), mappingsCaptor.capture());
  }

  private ProteinMapping getProteinMapping(String proteinId, String geneName) {
    ProteinMapping mapping = new ProteinMapping();
    mapping.setProteinId(proteinId);
    mapping.setGenes(Arrays.asList(new GeneInfo(1L, geneName)));
    return mapping;
  }
}
