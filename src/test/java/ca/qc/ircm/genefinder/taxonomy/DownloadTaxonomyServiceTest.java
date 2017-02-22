/*
 * Copyright (c) 2006 Institut de recherches cliniques de Montreal (IRCM)
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

package ca.qc.ircm.genefinder.taxonomy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import ca.qc.ircm.genefinder.annotation.NcbiConfiguration;
import ca.qc.ircm.genefinder.ftp.FtpService;
import ca.qc.ircm.genefinder.test.config.ServiceTestAnnotations;
import ca.qc.ircm.progressbar.ProgressBar;
import org.apache.commons.net.ftp.FTPClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class DownloadTaxonomyServiceTest {
  private DownloadTaxonomyService downloadTaxonomyService;
  @Mock
  private NcbiConfiguration ncbiConfiguration;
  @Mock
  private FtpService ftpService;
  @Mock
  private FTPClient ftpClient;
  @Mock
  private ProgressBar progressBar;
  @Captor
  private ArgumentCaptor<List<Taxon>> taxonomyCaptor;
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private Locale locale;
  private String ftpUrl;
  private String taxonomyUrl;
  private String taxonomyNodesFilename;
  private Path localFile;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() throws Throwable {
    downloadTaxonomyService = new DownloadTaxonomyService(ncbiConfiguration, ftpService);
    locale = Locale.getDefault();
    ftpUrl = "localhost:8080";
    taxonomyUrl = "/taxdump.zip";
    taxonomyNodesFilename = "nodes.dmp";
    localFile = temporaryFolder.getRoot().toPath().resolve("local_taxonomy.zip");
    when(ncbiConfiguration.ftp()).thenReturn(ftpUrl);
    when(ncbiConfiguration.taxonomy()).thenReturn(taxonomyUrl);
    when(ncbiConfiguration.taxonomyNodes()).thenReturn(taxonomyNodesFilename);
    when(ftpService.anonymousConnect(any())).thenReturn(ftpClient);
    when(progressBar.step(anyDouble())).thenReturn(progressBar);
    doAnswer(i -> {
      System.out.println(i.getArguments()[0]);
      return null;
    }).when(progressBar).setMessage(any());
  }

  private Path taxonomyFile() throws IOException, URISyntaxException {
    Path taxonomyNames = Paths.get(getClass().getResource("/names.dmp").toURI());
    Path taxonomyNodes = Paths.get(getClass().getResource("/nodes.dmp").toURI());
    Path taxonomy = temporaryFolder.newFile("taxonomy.zip").toPath();
    try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(taxonomy))) {
      zip.putNextEntry(new ZipEntry(taxonomyNames.getFileName().toString()));
      Files.copy(taxonomyNames, zip);
      zip.putNextEntry(new ZipEntry(taxonomyNodes.getFileName().toString()));
      Files.copy(taxonomyNodes, zip);
    }
    return taxonomy;
  }

  @Test
  public void children_9604() throws Throwable {
    Path taxonomy = taxonomyFile();
    when(ftpService.localFile(any())).thenReturn(localFile);
    doAnswer(i -> {
      Path output = (Path) i.getArguments()[2];
      Files.copy(taxonomy, output);
      return null;
    }).when(ftpService).downloadFile(any(), any(), any(), any(), any());

    Set<Integer> children = downloadTaxonomyService.children(9604, progressBar, locale);

    assertFalse(children.contains(9604));
    assertTrue(children.contains(207598));
    assertTrue(children.contains(9605));
    assertTrue(children.contains(9606));
    assertFalse(children.contains(314295));
    assertFalse(children.contains(1));
  }

  @Test
  public void children_4930() throws Throwable {
    Path taxonomy = taxonomyFile();
    when(ftpService.localFile(any())).thenReturn(localFile);
    doAnswer(i -> {
      Path output = (Path) i.getArguments()[2];
      Files.copy(taxonomy, output);
      return null;
    }).when(ftpService).downloadFile(any(), any(), any(), any(), any());

    Set<Integer> children = downloadTaxonomyService.children(4930, progressBar, locale);

    assertFalse(children.contains(4930));
    assertTrue(children.contains(4932));
    assertTrue(children.contains(1337652));
    assertTrue(children.contains(559292));
    assertTrue(children.contains(580240));
    assertFalse(children.contains(4893));
    assertFalse(children.contains(1));
  }
}
