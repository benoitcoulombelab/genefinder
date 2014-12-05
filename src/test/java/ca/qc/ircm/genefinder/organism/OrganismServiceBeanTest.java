package ca.qc.ircm.genefinder.organism;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import ca.qc.ircm.genefinder.ApplicationProperties;
import ca.qc.ircm.genefinder.organism.Organism;
import ca.qc.ircm.genefinder.organism.OrganismServiceBean;
import ca.qc.ircm.genefinder.test.config.TestLoggingRunner;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@RunWith(TestLoggingRunner.class)
public class OrganismServiceBeanTest {
    private OrganismServiceBean organismServiceBean;
    @Mock
    private ApplicationProperties applicationProperties;
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private File data;

    @Before
    public void beforeTest() throws Throwable {
        organismServiceBean = new OrganismServiceBean(applicationProperties);
        data = temporaryFolder.newFile("organisms.json");
        File originalData = new File(getClass().getResource("/organism/organisms.json").toURI());
        FileUtils.copyFile(originalData, data);
        when(applicationProperties.getOrganismData()).thenReturn(data);
    }

    private List<Organism> load() throws IOException {
        List<Organism> organisms;
        Gson gson = new Gson();
        Type collectionType = new TypeToken<Collection<Organism>>() {
        }.getType();
        try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(data), "UTF-8"))) {
            organisms = gson.fromJson(reader, collectionType);
        }
        return organisms;
    }

    @Test
    public void get() {
        Organism organism = organismServiceBean.get(9606);
        assertEquals((Integer) 9606, organism.getId());
        assertEquals("Homo Sapiens", organism.getName());
    }

    @Test
    public void get_Null() {
        assertNull(organismServiceBean.get(null));
    }

    @Test
    public void insert() throws Throwable {
        Organism organism = new Organism(9796, "Equus caballus");

        organismServiceBean.insert(organism);

        List<Organism> organisms = load();
        assertEquals(3, organisms.size());
        organism = organisms.get(0);
        assertEquals((Integer) 9606, organism.getId());
        assertEquals("Homo Sapiens", organism.getName());
        organism = organisms.get(1);
        assertEquals((Integer) 10090, organism.getId());
        assertEquals("Mus Musculus", organism.getName());
        organism = organisms.get(2);
        assertEquals((Integer) 9796, organism.getId());
        assertEquals("Equus caballus", organism.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void insert_Duplicate() throws Throwable {
        Organism organism = new Organism(9606, "Homo Sapiens");

        organismServiceBean.insert(organism);
    }

    @Test
    public void insert_ThenAll() throws Throwable {
        Organism organism = new Organism(9796, "Equus caballus");
        organismServiceBean.all();

        organismServiceBean.insert(organism);

        List<Organism> organisms = organismServiceBean.all();
        assertEquals(3, organisms.size());
        organism = organisms.get(0);
        assertEquals((Integer) 9606, organism.getId());
        assertEquals("Homo Sapiens", organism.getName());
        organism = organisms.get(1);
        assertEquals((Integer) 10090, organism.getId());
        assertEquals("Mus Musculus", organism.getName());
        organism = organisms.get(2);
        assertEquals((Integer) 9796, organism.getId());
        assertEquals("Equus caballus", organism.getName());
        assertNotNull(organismServiceBean.get(9606));
        assertNotNull(organismServiceBean.get(10090));
        assertNotNull(organismServiceBean.get(9796));
    }

    @Test
    public void delete_One() throws Throwable {
        List<Organism> organisms = new ArrayList<Organism>();
        organisms.add(new Organism(9606, "Homo Sapiens"));

        organismServiceBean.delete(organisms);

        organisms = load();
        assertEquals(1, organisms.size());
        Organism organism = organisms.get(0);
        assertEquals((Integer) 10090, organism.getId());
        assertEquals("Mus Musculus", organism.getName());
    }

    @Test
    public void delete_Multiple() throws Throwable {
        List<Organism> organisms = new ArrayList<Organism>();
        organisms.add(new Organism(9606, "Homo Sapiens"));
        organisms.add(new Organism(10090, "Mus Musculus"));

        organismServiceBean.delete(organisms);

        organisms = load();
        assertEquals(0, organisms.size());
    }

    @Test
    public void delete_ThenAll() throws Throwable {
        List<Organism> organisms = new ArrayList<Organism>();
        organisms.add(new Organism(9606, "Homo Sapiens"));
        organismServiceBean.all();

        organismServiceBean.delete(organisms);

        organisms = organismServiceBean.all();
        assertEquals(1, organisms.size());
        Organism organism = organisms.get(0);
        assertEquals((Integer) 10090, organism.getId());
        assertEquals("Mus Musculus", organism.getName());
        assertNotNull(organismServiceBean.get(10090));
        assertNull(organismServiceBean.get(9606));
    }
}