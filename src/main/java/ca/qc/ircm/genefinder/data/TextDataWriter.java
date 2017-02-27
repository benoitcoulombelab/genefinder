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
import java.util.stream.Collectors;

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
          String newValue = proteinIds.stream().filter(proteinId -> mappings.get(proteinId) != null)
              .map(proteinId -> mappings.get(proteinId).getGenes()).filter(genes -> genes != null)
              .flatMap(genes -> genes.stream()).map(gene -> String.valueOf(gene.getId())).distinct()
              .collect(Collectors.joining(PROTEIN_DELIMITER));
          writer.write(newValue);
        }
        if (parameters.isGeneName()) {
          writer.write("\t");
          String newValue = proteinIds.stream().filter(proteinId -> mappings.get(proteinId) != null)
              .map(proteinId -> mappings.get(proteinId).getGenes()).filter(genes -> genes != null)
              .flatMap(genes -> genes.stream()).map(gene -> gene.getSymbol()).filter(s -> s != null)
              .distinct().collect(Collectors.joining(PROTEIN_DELIMITER));
          writer.write(newValue);
        }
        if (parameters.isGeneSynonyms()) {
          writer.write("\t");
          String newValue = proteinIds.stream().filter(proteinId -> mappings.get(proteinId) != null)
              .map(proteinId -> mappings.get(proteinId).getGenes()).filter(genes -> genes != null)
              .flatMap(genes -> genes.stream()).map(gene -> gene.getSynonyms())
              .filter(s -> s != null)
              .map(s -> s.stream().collect(Collectors.joining(LIST_DELIMITER))).distinct()
              .collect(Collectors.joining(PROTEIN_DELIMITER));
          writer.write(newValue);
        }
        if (parameters.isGeneSummary()) {
          writer.write("\t");
          String newValue = proteinIds.stream().filter(proteinId -> mappings.get(proteinId) != null)
              .map(proteinId -> mappings.get(proteinId).getGenes()).filter(genes -> genes != null)
              .flatMap(genes -> genes.stream()).map(gene -> gene.getDescription())
              .filter(s -> s != null).distinct().collect(Collectors.joining(PROTEIN_DELIMITER));
          writer.write(newValue);
        }
        if (parameters.isProteinMolecularWeight()) {
          writer.write("\t");
          String newValue = proteinIds.stream().filter(proteinId -> mappings.get(proteinId) != null)
              .map(proteinId -> mappings.get(proteinId).getMolecularWeight())
              .filter(mw -> mw != null).map(mw -> numberFormat.format(mw))
              .collect(Collectors.joining(PROTEIN_DELIMITER));
          writer.write(newValue);
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
