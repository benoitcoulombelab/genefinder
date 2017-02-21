package ca.qc.ircm.genefinder.data;

import ca.qc.ircm.genefinder.annotation.ProteinMapping;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.stereotype.Component;

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

@Component
public class TextDataWriter extends AbstractDataWriter implements DataWriter {
  private static final NumberFormat numberFormat;

  static {
    numberFormat = NumberFormat.getNumberInstance(Locale.ENGLISH);
    numberFormat.setMinimumFractionDigits(1);
    numberFormat.setGroupingUsed(false);
  }

  @Override
  public void writeGene(File input, File output, FindGenesParameters parameters,
      Map<String, ProteinMapping> mappings) throws IOException, InterruptedException {
    try (
        LineNumberReader reader =
            new LineNumberReader(new InputStreamReader(new FileInputStream(input)));
        BufferedWriter writer =
            new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output)))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] columns = line.split("\t", -1);
        List<String> proteinIds = parseProteinIds(columns[parameters.getProteinColumn()]);
        for (int i = 0; i <= parameters.getProteinColumn(); i++) {
          if (i > 0) {
            writer.write("\t");
          }
          writer.write(columns[i]);
        }
        if (parameters.isGeneId()) {
          writer.write("\t");
          writer.write(formatCollection(proteinIds,
              proteinId -> mappings.get(proteinId) != null
                  && mappings.get(proteinId).getGeneId() != null
                      ? mappings.get(proteinId).getGeneId().toString() : ""));
        }
        if (parameters.isGeneName()) {
          writer.write("\t");
          writer.write(formatCollection(proteinIds,
              proteinId -> mappings.get(proteinId) != null
                  && mappings.get(proteinId).getGeneName() != null
                      ? mappings.get(proteinId).getGeneName() : ""));
        }
        if (parameters.isGeneSynonyms()) {
          writer.write("\t");
          writer.write(formatCollection(proteinIds,
              proteinId -> mappings.get(proteinId) != null
                  && mappings.get(proteinId).getGeneSynonyms() != null
                      ? mappings.get(proteinId).getGeneSynonyms() : ""));
        }
        if (parameters.isGeneSummary()) {
          writer.write("\t");
          writer.write(formatCollection(proteinIds,
              proteinId -> mappings.get(proteinId) != null
                  && mappings.get(proteinId).getGeneSummary() != null
                      ? mappings.get(proteinId).getGeneSummary() : ""));
        }
        if (parameters.isProteinMolecularWeight()) {
          writer.write("\t");
          writer.write(formatCollection(proteinIds,
              proteinId -> mappings.get(proteinId) != null
                  && mappings.get(proteinId).getMolecularWeight() != null
                      ? numberFormat.format(mappings.get(proteinId).getMolecularWeight()) : ""));
        }
        for (int i = parameters.getProteinColumn() + 1; i < columns.length; i++) {
          writer.write("\t");
          writer.write(columns[i]);
        }
        writer.write(SystemUtils.LINE_SEPARATOR);
      }
    }
  }
}
