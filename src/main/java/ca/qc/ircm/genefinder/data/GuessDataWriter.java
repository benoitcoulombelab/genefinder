package ca.qc.ircm.genefinder.data;

import ca.qc.ircm.genefinder.ncbi.ProteinMapping;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

import javax.inject.Inject;

public class GuessDataWriter implements DataWriter {
  private static final Pattern EXCEL_FILENAME_PATTERN = Pattern.compile(".+\\.xls(\\w?)");

  @Inject
  private ExcelDataWriter excelDataWriter;
  @Inject
  private TextDataWriter textDataWriter;

  protected GuessDataWriter() {
  }

  public GuessDataWriter(ExcelDataWriter excelDataWriter, TextDataWriter textDataWriter) {
    this.excelDataWriter = excelDataWriter;
    this.textDataWriter = textDataWriter;
  }

  @Override
  public void writeGene(File input, File output, FindGenesParameters parameters,
      Map<Integer, ProteinMapping> mappings) throws IOException, InterruptedException {
    if (EXCEL_FILENAME_PATTERN.matcher(input.getName()).matches()) {
      excelDataWriter.writeGene(input, output, parameters, mappings);
    } else {
      textDataWriter.writeGene(input, output, parameters, mappings);
    }
  }
}
