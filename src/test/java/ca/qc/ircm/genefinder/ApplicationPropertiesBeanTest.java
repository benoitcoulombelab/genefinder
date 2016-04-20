package ca.qc.ircm.genefinder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import ca.qc.ircm.genefinder.test.config.Rules;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Tests for {@link ApplicationPropertiesBean}.
 */
public class ApplicationPropertiesBeanTest {
  private ApplicationPropertiesBean applicationPropertiesBean;
  private String originalUserhome;
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  @Rule
  public RuleChain rules = Rules.defaultRules(this).around(temporaryFolder);

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    originalUserhome = System.getProperty("user.home");
    System.setProperty("user.home", temporaryFolder.getRoot().getPath());
    applicationPropertiesBean = new ApplicationPropertiesBean();
    applicationPropertiesBean.init();
  }

  @After
  public void afterTest() {
    System.setProperty("user.home", originalUserhome);
  }

  private Path home() {
    return temporaryFolder.getRoot().toPath().resolve("genefinder");
  }

  @Test
  public void getHome() {
    assertEquals(Paths.get(System.getProperty("user.home") + "/genefinder"),
        applicationPropertiesBean.getHome());
  }

  @Test
  public void getAnnotationsFolder() throws Throwable {
    Path annotationsHome = home().resolve("annotations");
    Files.createDirectories(annotationsHome);

    assertEquals(annotationsHome, applicationPropertiesBean.getAnnotationsFolder());
  }

  @Test
  public void getAnnotationsFolder_CreateDirectory() {
    Path annotationsHome = home().resolve("annotations");

    applicationPropertiesBean.getAnnotationsFolder();

    assertTrue(Files.isDirectory(annotationsHome));
  }

  @Test(expected = IllegalStateException.class)
  public void getAnnotationsFolder_DirectoryIsFile() throws Throwable {
    Path annotationsHome = home().resolve("annotations");
    Files.createDirectories(annotationsHome.getParent());
    Files.createFile(annotationsHome);

    applicationPropertiesBean.getAnnotationsFolder();
  }

  @Test
  public void getOrganismData() {
    assertEquals(Paths.get(System.getProperty("user.home") + "/genefinder/organisms.json"),
        applicationPropertiesBean.getOrganismData());
  }

  @Test
  public void getProperty() {
    assertEquals("ftp://ftp.ncbi.nih.gov/gene/DATA/gene2accession.gz",
        applicationPropertiesBean.getProperty("ncbi.gene2accession"));
  }

  @Test
  public void getProperty_Default() {
    assertEquals(null, applicationPropertiesBean.getProperty("unit_test"));
    assertEquals("abc", applicationPropertiesBean.getProperty("unit_test", "abc"));
    assertEquals("ftp://ftp.ncbi.nih.gov/gene/DATA/gene2accession.gz",
        applicationPropertiesBean.getProperty("ncbi.gene2accession", "abc"));
  }
}
