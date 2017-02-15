package ca.qc.ircm.genefinder.organism;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import ca.qc.ircm.genefinder.ApplicationProperties;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.ref.SoftReference;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

@Service
public class OrganismServiceBean implements OrganismService {
  private static final Logger logger = LoggerFactory.getLogger(OrganismServiceBean.class);
  @Inject
  private ApplicationProperties applicationProperties;
  private SoftReference<List<Organism>> organismsCache;
  private SoftReference<Map<Integer, Organism>> organismsByIdCache;

  protected OrganismServiceBean() {
  }

  public OrganismServiceBean(ApplicationProperties applicationProperties) {
    this.applicationProperties = applicationProperties;
  }

  private Path getDataFile() {
    return applicationProperties.getOrganismData();
  }

  private List<Organism> getOrganisms() throws IOException {
    Path dataFile = getDataFile();
    synchronized (dataFile) {
      List<Organism> organisms = null;
      if (organismsCache != null && organismsCache.get() != null) {
        organisms = organismsCache.get();
      }
      if (organisms == null) {
        try {
          organisms = load(dataFile);
        } catch (IOException e) {
          // Don't throw exception before trying a reset.
          organisms = null;
        }
        if (organisms == null) {
          reset(dataFile);
          organisms = load(dataFile);
        }
      }
      return organisms;
    }
  }

  private List<Organism> load(Path dataFile) throws IOException {
    List<Organism> organisms;
    Gson gson = new Gson();
    Type collectionType = new TypeToken<Collection<Organism>>() {}.getType();
    try (Reader reader = Files.newBufferedReader(dataFile, Charset.forName("UTF-8"))) {
      organisms = gson.fromJson(reader, collectionType);
    } catch (JsonParseException e) {
      organisms = null;
    }
    List<Organism> cacheOrganisms = organisms != null ? organisms : Collections.emptyList();
    organismsCache = new SoftReference<>(cacheOrganisms);
    organismsByIdCache = new SoftReference<>(
        cacheOrganisms.stream().collect(Collectors.toMap(Organism::getId, Function.identity())));
    return organisms;
  }

  private void reset(Path dataFile) throws IOException {
    try (
        InputStream input =
            new BufferedInputStream(getClass().getResourceAsStream("/organisms.json"));
        OutputStream output = new BufferedOutputStream(Files.newOutputStream(dataFile))) {
      IOUtils.copy(input, output);
    }
  }

  private void save(Collection<Organism> organisms, Path dataFile) throws IOException {
    Gson gson = new Gson();
    try (Writer writer = Files.newBufferedWriter(dataFile, Charset.forName("UTF-8"))) {
      gson.toJson(organisms, writer);
    }
  }

  @Override
  public Organism get(Integer id) {
    if (organismsByIdCache != null) {
      Map<Integer, Organism> organisms = organismsByIdCache.get();
      if (organisms != null) {
        return organisms.get(id);
      }
    }
    try {
      List<Organism> organisms = getOrganisms();
      return organisms.stream().filter(organism -> organism.getId().equals(id)).findFirst()
          .orElse(null);
    } catch (IOException e) {
      logger.error("Could not read organism data file", e);
      return null;
    }
  }

  @Override
  public List<Organism> all() {
    try {
      return getOrganisms();
    } catch (IOException e) {
      logger.error("Could not read organism data file", e);
      return new ArrayList<>();
    }
  }

  @Override
  public void insert(Organism organism) {
    if (get(organism.getId()) != null) {
      throw new IllegalArgumentException(
          Organism.class.getSimpleName() + " " + organism.getId() + " already exists");
    }
    try {
      Path dataFile = getDataFile();
      synchronized (dataFile) {
        List<Organism> organisms = load(dataFile);
        organisms.add(organism);
        save(organisms, dataFile);
      }
      load(dataFile);
    } catch (IOException e) {
      logger.error("Could not read/write organism data file", e);
    }
  }

  @Override
  public void delete(Collection<Organism> organisms) {
    try {
      Path dataFile = getDataFile();
      synchronized (dataFile) {
        List<Organism> allOrganisms = load(dataFile);
        allOrganisms.removeAll(organisms);
        save(allOrganisms, dataFile);
      }
      load(dataFile);
    } catch (IOException e) {
      logger.error("Could not read/write organism data file", e);
    }
  }
}
