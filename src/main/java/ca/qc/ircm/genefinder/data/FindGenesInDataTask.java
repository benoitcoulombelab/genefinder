package ca.qc.ircm.genefinder.data;

import java.io.File;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.concurrent.Task;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.qc.ircm.genefinder.organism.Organism;
import ca.qc.ircm.progress_bar.ProgressBar;
import ca.qc.ircm.progress_bar.SimpleProgressBar;

import com.google.inject.assistedinject.Assisted;

/**
 * Task that download protein database and find genes for MaxQuant file.
 */
public class FindGenesInDataTask extends Task<Map<File, File>> {
    private class TaskProgessBar extends SimpleProgressBar {
        private static final long serialVersionUID = -7524127914872361603L;

        @Override
        public void setProgress(double progress) {
            super.setProgress(progress);
            updateProgress(progress, Math.max(progress, 1.0));
        }

        @Override
        public void setMessage(String message) {
            super.setMessage(message);
            updateMessage(message);
            logger.trace("updateMessage {}", message);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(FindGenesInDataTask.class);
    private final ResourceBundle bundle;
    private Organism organism;
    private DataService dataService;
    private Collection<File> files;
    private FindGenesParameters findGenesParameter;
    private Locale locale;

    @Inject
    public FindGenesInDataTask(@Assisted Organism organism, DataService dataService, @Assisted Collection<File> files,
            @Assisted FindGenesParameters findGenesParameter, @Assisted Locale locale) {
        this.organism = organism;
        this.dataService = dataService;
        this.files = files;
        this.findGenesParameter = findGenesParameter;
        this.locale = locale;
        bundle = ResourceBundle.getBundle(FindGenesInDataTask.class.getName(), locale);
    }

    @Override
    protected Map<File, File> call() throws Exception {
        ProgressBar progressBar = new TaskProgessBar();
        Map<File, File> results = new HashMap<File, File>();
        logger.debug("fillGeneDatabase {}", organism);
        double step = 0.4 / Math.max(files.size(), 1);
        for (File file : files) {
            logger.debug("findGeneNames {}", file);
            File withGenes = dataService.findGeneNames(organism, file, findGenesParameter, progressBar.step(step),
                    locale);
            String extension = FilenameUtils.getExtension(file.getName());
            String filename = MessageFormat.format(bundle.getString("output.filename"),
                    FilenameUtils.getBaseName(file.getName()), extension.isEmpty() ? 0 : 1, extension);
            File output = new File(file.getParentFile(), filename);
            if (output.exists()) {
                if (!output.delete())
                    logger.warn("Could not deleted destination file {}", output);
            }
            logger.debug("move {} to {}", withGenes, output);
            FileUtils.moveFile(withGenes, output);
            results.put(file, output);
        }
        logger.debug("completed files {}", files);
        return results;
    }
}
