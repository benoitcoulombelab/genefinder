package ca.qc.ircm.genefinder;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import ca.qc.ircm.genefinder.test.config.TestLoggingRunner;

/**
 * Tests for {@link ApplicationPropertiesBean}.
 */
@RunWith(TestLoggingRunner.class)
public class ApplicationPropertiesBeanTest {
    private ApplicationPropertiesBean applicationPropertiesBean;

    @Before
    public void beforeTest() {
        applicationPropertiesBean = new ApplicationPropertiesBean();
        applicationPropertiesBean.init();
    }

    @Test
    public void getHome() {
        assertEquals(new File(System.getProperty("user.home") + "/genefinder"), applicationPropertiesBean.getHome());
    }

    @Test
    public void getOrganismData() {
        assertEquals(new File(System.getProperty("user.home") + "/genefinder/organisms.json"),
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
