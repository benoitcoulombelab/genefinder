package ca.qc.ircm.genefinder.data;

import ca.qc.ircm.genefinder.annotation.ProteinMapping;
import ca.qc.ircm.genefinder.annotation.DownloadProteinMappingService;
import ca.qc.ircm.genefinder.organism.Organism;
import ca.qc.ircm.genefinder.util.ExceptionUtils;
import ca.qc.ircm.progressbar.ProgressBar;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

/**
 * Services for data file.
 */
@Service
public class DataService {
  @Inject
  private DownloadProteinMappingService downloadProteinMappingService;
  @Inject
  private DataWriter dataWriter;

  protected DataService() {
  }

  protected DataService(DownloadProteinMappingService downloadProteinMappingService, DataWriter dataWriter) {
    this.downloadProteinMappingService = downloadProteinMappingService;
    this.dataWriter = dataWriter;
  }

  public void findGeneNames(Organism organism, Collection<File> files,
      FindGenesParameters parameters, ProgressBar progressBar, Locale locale)
      throws IOException, InterruptedException {
    ResourceBundle bundle = ResourceBundle.getBundle(DataService.class.getName(), locale);
    progressBar.setMessage(MessageFormat.format(bundle.getString("mappings"), organism.getName()));
    ExceptionUtils.throwIfInterrupted("Interrupted gene finding");
    List<ProteinMapping> rawMappings =
        downloadProteinMappingService.allProteinMappings(organism, progressBar.step(0.8), locale);
    Map<String, ProteinMapping> mappings = rawMappings.stream().collect(
        Collectors.toMap(ProteinMapping::getProteinId, Function.<ProteinMapping>identity()));
    progressBar.setProgress(0.8);
    double step = 0.2 / Math.max(files.size(), 1);
    int count = 0;
    for (File file : files) {
      progressBar.setMessage(MessageFormat.format(bundle.getString("finding"), file.getName()));
      String extension = FilenameUtils.getExtension(file.getName());
      String filename = MessageFormat.format(bundle.getString("output.filename"),
          FilenameUtils.getBaseName(file.getName()), extension.isEmpty() ? 0 : 1, extension);
      File output = new File(file.getParentFile(), filename);
      dataWriter.writeGene(file, output, parameters, mappings);
      progressBar.setProgress(0.5 + 0.5 * step * count++);
    }
    progressBar.setProgress(1.0);
  }
}
