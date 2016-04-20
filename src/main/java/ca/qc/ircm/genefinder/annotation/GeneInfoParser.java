package ca.qc.ircm.genefinder.annotation;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Parses NCBI gene info files.
 */
public class GeneInfoParser {
  private static final String DATE_FORMAT_PATTERN = "yyyyMMdd";

  /**
   * Parses gene info file.
   *
   * @param reader
   *          reader of gene info file
   * @return parsed gene infos
   * @throws IOException
   *           could not parse file
   */
  public List<GeneInfo> parse(BufferedReader reader) throws IOException {
    Accumulator accumulator = new Accumulator();
    parse(reader, accumulator);
    return accumulator.geneInfos;
  }

  /**
   * Parses gene info file.
   *
   * @param reader
   *          reader of gene info file
   * @param handler
   *          handles gene infos
   * @throws IOException
   *           could not parse file
   */
  public void parse(BufferedReader reader, Consumer<GeneInfo> handler) throws IOException {
    parse(reader, new ConsumerBiWrapper(handler));
  }

  /**
   * Parses gene info file.
   *
   * @param reader
   *          reader of gene info file
   * @param handler
   *          handles gene infos
   * @throws IOException
   *           could not parse file
   */
  public void parse(BufferedReader reader, BiConsumer<GeneInfo, String> handler)
      throws IOException {
    String line;
    while ((line = reader.readLine()) != null) {
      if (line.isEmpty() || line.startsWith("#")) {
        continue;
      }

      String[] columns = line.split("\t", -1);
      GeneInfo geneInfo = new GeneInfo();
      geneInfo.setOrganismId(Integer.parseInt(columns[0]));
      geneInfo.setId(Integer.parseInt(columns[1]));
      String symbol = columns[2];
      if (!symbol.isEmpty() && !symbol.equals("-")) {
        geneInfo.setSymbol(symbol);
      }
      String locusTag = columns[3];
      if (!locusTag.isEmpty() && !locusTag.equals("-")) {
        geneInfo.setLocusTag(locusTag);
      }
      String synonymns = columns[4];
      if (!synonymns.isEmpty() && !synonymns.equals("-")) {
        geneInfo.setSynonyms(new ArrayList<>(Arrays.asList(synonymns.split("\\|", -1))));
      } else {
        geneInfo.setSynonyms(new ArrayList<String>());
      }
      String dbXrefs = columns[5];
      if (!dbXrefs.isEmpty() && !dbXrefs.equals("-")) {
        geneInfo.setDbXrefs(new ArrayList<>(Arrays.asList(dbXrefs.split("\\|", -1))));
      } else {
        geneInfo.setDbXrefs(new ArrayList<String>());
      }
      String chromosome = columns[6];
      if (!chromosome.isEmpty() && !chromosome.equals("-")) {
        geneInfo.setChromosome(chromosome);
      }
      String mapLocation = columns[7];
      if (!mapLocation.isEmpty() && !mapLocation.equals("-")) {
        geneInfo.setMapLocation(mapLocation);
      }
      String description = columns[8];
      if (!description.isEmpty() && !description.equals("-")) {
        geneInfo.setDescription(description);
      }
      String typeOfGene = columns[9];
      if (!typeOfGene.isEmpty() && !typeOfGene.equals("-")) {
        geneInfo.setTypeOfGene(typeOfGene);
      }
      String symbolFromNomenclatureAuthority = columns[10];
      if (!symbolFromNomenclatureAuthority.isEmpty()
          && !symbolFromNomenclatureAuthority.equals("-")) {
        geneInfo.setSymbolFromNomenclatureAuthority(symbolFromNomenclatureAuthority);
      }
      String fullNameFromNomenclatureAuthority = columns[11];
      if (!fullNameFromNomenclatureAuthority.isEmpty()
          && !fullNameFromNomenclatureAuthority.equals("-")) {
        geneInfo.setFullNameFromNomenclatureAuthority(fullNameFromNomenclatureAuthority);
      }
      String nomenclatureStatus = columns[12];
      if (!nomenclatureStatus.isEmpty() && !nomenclatureStatus.equals("-")) {
        geneInfo.setNomenclatureStatus(nomenclatureStatus);
      }
      String otherDesignations = columns[13];
      if (!otherDesignations.isEmpty() && !otherDesignations.equals("-")) {
        geneInfo.setOtherDesignations(
            new ArrayList<>(Arrays.asList(otherDesignations.split("\\|", -1))));
      } else {
        geneInfo.setOtherDesignations(new ArrayList<String>());
      }
      String modificationDate = columns[14];
      if (!modificationDate.isEmpty() && !modificationDate.equals("-")) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN);
        try {
          geneInfo.setModificationDate(dateFormat.parse(modificationDate));
        } catch (ParseException e) {
          // Ignore.
        }
      }
      handler.accept(geneInfo, line);
    }
  }

  private static class Accumulator implements Consumer<GeneInfo> {
    private List<GeneInfo> geneInfos = new ArrayList<>();

    @Override
    public void accept(GeneInfo input) {
      geneInfos.add(input);
    }
  }

  private static class ConsumerBiWrapper implements BiConsumer<GeneInfo, String> {
    private final Consumer<GeneInfo> consumer;

    ConsumerBiWrapper(Consumer<GeneInfo> consumer) {
      this.consumer = consumer;
    }

    @Override
    public void accept(GeneInfo input, String line) {
      consumer.accept(input);
    }
  }
}
