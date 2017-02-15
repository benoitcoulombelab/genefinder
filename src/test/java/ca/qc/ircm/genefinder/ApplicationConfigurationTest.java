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
  private Path originalHome;

  @Before
  public void beforeTest() {
    originalHome = applicationConfiguration.home();
  }

  @After
  public void afterTest() {
    setDataFolder(originalHome);
  }

  private void setDataFolder(Path dataFolder) {
    ((ApplicationConfigurationSpringBoot) applicationConfiguration).setHome(dataFolder);
  }

  private void setTemporaryFolderAsDataFolder() {
    setDataFolder(temporaryFolder.getRoot().toPath());
  }

  private Path getDataFolder() {
    return temporaryFolder.getRoot().toPath();
  }

  @Test
  public void defaultProperties() throws Throwable {
    assertEquals(Paths.get(System.getProperty("user.dir")), originalHome);
  }

  @Test
  public void annotationsFolder() throws Throwable {
    setTemporaryFolderAsDataFolder();
    Path annotationsHome = getDataFolder().resolve("annotations");
    Files.createDirectories(annotationsHome);

    assertEquals(annotationsHome, applicationConfiguration.annotationsFolder());
  }

  @Test
  public void annotationsFolder_CreateDirectory() {
    setTemporaryFolderAsDataFolder();
    Path annotationsHome = getDataFolder().resolve("annotations");

    applicationConfiguration.annotationsFolder();

    assertTrue(Files.isDirectory(annotationsHome));
  }

  @Test(expected = IllegalStateException.class)
  public void annotationsFolder_DirectoryIsFile() throws Throwable {
    setTemporaryFolderAsDataFolder();
    Path annotationsHome = getDataFolder().resolve("annotations");
    Files.createFile(annotationsHome);

    applicationConfiguration.annotationsFolder();
  }
}
