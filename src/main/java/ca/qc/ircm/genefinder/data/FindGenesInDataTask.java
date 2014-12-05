package ca.qc.ircm.genefinder.data;

import java.io.File;
import java.util.Collection;
import java.util.Locale;

import javafx.concurrent.Task;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.qc.ircm.genefinder.organism.Organism;
import ca.qc.ircm.progress_bar.ProgressBar;
import ca.qc.ircm.progress_bar.SimpleProgressBar;

import com.google.inject.assistedinject.Assisted;

/**
 * Task that download protein database and find genes for MaxQuant file.
 */
public class FindGenesInDataTask extends Task<Void> {
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
    }

    @Override
    protected Void call() throws Exception {
        ProgressBar progressBar = new TaskProgessBar();
        dataService.findGeneNames(organism, files, findGenesParameter, progressBar, locale);
        logger.debug("completed files {}", files);
        return null;
    }
}
