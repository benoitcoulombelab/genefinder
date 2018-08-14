/*
 * Copyright (c) 2014 Institut de recherches cliniques de Montreal (IRCM)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ca.qc.ircm.genefinder.data;

import ca.qc.ircm.genefinder.annotation.NcbiConfiguration;
import ca.qc.ircm.genefinder.annotation.ProteinMapping;
import ca.qc.ircm.genefinder.annotation.UniprotConfiguration;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.stereotype.Component;

@Component
public class TextDataWriter extends AbstractDataWriter implements DataWriter {
  private static final NumberFormat numberFormat;

  static {
    numberFormat = NumberFormat.getNumberInstance(Locale.ENGLISH);
    numberFormat.setMinimumFractionDigits(1);
    numberFormat.setGroupingUsed(false);
  }

  protected TextDataWriter() {
    super();
  }

  protected TextDataWriter(NcbiConfiguration ncbiConfiguration,
      UniprotConfiguration uniprotConfiguration) {
    super(ncbiConfiguration, uniprotConfiguration);
  }

  @Override
  public void writeGene(File input, File output, FindGenesParameters parameters,
      Map<String, ProteinMapping> mappings) throws IOException, InterruptedException {
    Pattern proteinIdPattern = proteinIdPattern(parameters);
    try (
        LineNumberReader reader =
            new LineNumberReader(new InputStreamReader(new FileInputStream(input)));
        BufferedWriter writer =
            new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output)))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] columns = line.split("\t", -1);
        List<String> proteinIds =
            parseProteinIds(columns[parameters.getProteinColumn()], proteinIdPattern);
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
