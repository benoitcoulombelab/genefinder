package ca.qc.ircm.genefinder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import ca.qc.ircm.genefinder.test.config.ServiceTestAnnotations;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class ApplicationConfigurationTest {
  @Inject
  private ApplicationConfiguration applicationConfiguration;
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private Path originalDownload;

  @Before
  public void beforeTest() {
    originalDownload = applicationConfiguration.download();
  }

  @After
  public void afterTest() {
    setDownload(originalDownload);
  }

  private void setDownload(Path download) {
    ((ApplicationConfigurationSpringBoot) applicationConfiguration).setDownload(download);
  }

  @Test
  public void defaultProperties() throws Throwable {
    assertEquals(Paths.get(System.getProperty("user.dir")).resolve("downloads"),
        applicationConfiguration.download());
  }

  @Test
  public void download() throws Throwable {
    Path download = temporaryFolder.getRoot().toPath().resolve("download");
    setDownload(download);
    Files.createDirectories(download);

    assertEquals(download, applicationConfiguration.download());
  }

  @Test
  public void download_CreateDirectory() {
    Path download = temporaryFolder.getRoot().toPath().resolve("download");
    setDownload(download);

    applicationConfiguration.download();

    assertTrue(Files.isDirectory(download));
  }

  @Test(expected = IllegalStateException.class)
  public void download_DirectoryIsFile() throws Throwable {
    Path download = temporaryFolder.getRoot().toPath().resolve("download");
    setDownload(download);
    Files.createFile(download);

    applicationConfiguration.download();
  }
}
