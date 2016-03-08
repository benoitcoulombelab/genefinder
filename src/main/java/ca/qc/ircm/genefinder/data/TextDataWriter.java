package ca.qc.ircm.genefinder.data;

import ca.qc.ircm.genefinder.ncbi.ProteinMapping;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;

public class TextDataWriter extends AbstractDataWriter implements DataWriter {
  private static final Logger logger = LoggerFactory.getLogger(TextDataWriter.class);
  private static final NumberFormat numberFormat;

  static {
    numberFormat = NumberFormat.getNumberInstance(Locale.ENGLISH);
    numberFormat.setMinimumFractionDigits(1);
    numberFormat.setGroupingUsed(false);
  }

  @Override
  public void writeGene(File input, File output, FindGenesParameters parameters,
      Map<Integer, ProteinMapping> mappings) throws IOException, InterruptedException {
    Header header = parseHeader(input);
    if (!finishedHeader(header)) {
      logger.warn("Could not find GI column in file {}", input);
      return;
    }
    try (
        LineNumberReader reader =
            new LineNumberReader(new InputStreamReader(new FileInputStream(input)));
        BufferedWriter writer =
            new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output)))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] columns = line.split("\t", -1);
        List<Integer> gis = parseGis(columns[header.giColumnIndex]);
        for (int i = 0; i <= header.giColumnIndex; i++) {
          if (i > 0) {
            writer.write("\t");
          }
          writer.write(columns[i]);
        }
        if (parameters.isGeneId()) {
          writer.write("\t");
          writer.write(formatCollection(gis,
              gi -> mappings.get(gi) != null && mappings.get(gi).getGeneId() != null
                  ? mappings.get(gi).getGeneId().toString() : ""));
        }
        if (parameters.isGeneName()) {
          writer.write("\t");
          writer.write(formatCollection(gis,
              gi -> mappings.get(gi) != null && mappings.get(gi).getGeneName() != null
                  ? mappings.get(gi).getGeneName() : ""));
        }
        if (parameters.isGeneSynonyms()) {
          writer.write("\t");
          writer.write(formatCollection(gis,
              gi -> mappings.get(gi) != null && mappings.get(gi).getGeneSynonyms() != null
                  ? mappings.get(gi).getGeneSynonyms() : ""));
        }
        if (parameters.isGeneSummary()) {
          writer.write("\t");
          writer.write(formatCollection(gis,
              gi -> mappings.get(gi) != null && mappings.get(gi).getGeneSummary() != null
                  ? mappings.get(gi).getGeneSummary() : ""));
        }
        if (parameters.isProteinMolecularWeight()) {
          writer.write("\t");
          writer
              .write(
                  formatCollection(gis,
                      gi -> mappings.get(gi) != null
                          && mappings.get(gi).getMolecularWeight() != null
                              ? numberFormat.format(mappings.get(gi).getMolecularWeight()) : ""));
        }
        for (int i = header.giColumnIndex + 1; i < columns.length; i++) {
          writer.write("\t");
          writer.write(columns[i]);
        }
        writer.write(SystemUtils.LINE_SEPARATOR);
      }
    }
  }

  private Header parseHeader(File file) throws IOException {
    Header header = new Header();
    try (LineNumberReader reader =
        new LineNumberReader(new InputStreamReader(new FileInputStream(file)))) {
      String line;
      while ((line = reader.readLine()) != null && !finishedHeader(header)) {
        String[] columns = line.split("\t", -1);
        for (int i = 0; i < columns.length; i++) {
          Matcher matcher = GI_PATTERN.matcher(columns[i]);
          if (matcher.find()) {
            header.giColumnIndex = i;
            break;
          }
        }
      }
    }
    return header;
  }
}
