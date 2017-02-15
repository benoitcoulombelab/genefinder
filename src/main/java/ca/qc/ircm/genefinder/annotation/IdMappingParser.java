package ca.qc.ircm.genefinder.annotation;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

/**
 * Parses UniProt's id mapping files.
 */
@Component
public class IdMappingParser {
  /**
   * Parses id mapping file.
   *
   * @param reader
   *          reader of id mapping file
   * @return parsed id mappings
   * @throws IOException
   *           could not parse file
   */
  public List<IdMapping> parse(BufferedReader reader) throws IOException {
    Accumulator accumulator = new Accumulator();
    parse(reader, accumulator);
    return accumulator.mappings;
  }

  /**
   * Parses id mapping file.
   *
   * @param handler
   *          handles id mappings
   * @param reader
   *          reader of id mapping file
   * @throws IOException
   *           could not parse file
   */
  public void parse(BufferedReader reader, Consumer<IdMapping> handler) throws IOException {
    IdMapping current = new IdMapping();
    current.setMappings(new HashMap<String, List<String>>());
    String line;
    while ((line = reader.readLine()) != null) {
      String[] columns = line.split("\t", -1);
      String id = columns[0];
      if (!id.equals(current.getProtein())) {
        if (current.getProtein() != null) {
          handler.accept(current);
        }
        current = new IdMapping();
        current.setProtein(id);
        current.setMappings(new HashMap<String, List<String>>());
      }
      String type = columns[1];
      if (!current.getMappings().containsKey(type)) {
        current.getMappings().put(type, new ArrayList<String>());
      }
      current.getMappings().get(type).add(columns[2]);
    }
    if (current.getProtein() != null) {
      handler.accept(current);
    }
  }

  private static class Accumulator implements Consumer<IdMapping> {
    private List<IdMapping> mappings = new ArrayList<>();

    @Override
    public void accept(IdMapping input) {
      mappings.add(input);
    }
  }
}
