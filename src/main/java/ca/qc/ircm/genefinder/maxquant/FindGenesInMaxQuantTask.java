package ca.qc.ircm.genefinder.maxquant;

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
public class FindGenesInMaxQuantTask extends Task<Map<File, File>> {
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

    private static final Logger logger = LoggerFactory.getLogger(FindGenesInMaxQuantTask.class);
    private final ResourceBundle bundle;
    private Organism organism;
    private MaxQuantService maxQuantService;
    private Collection<File> proteinGroupsFiles;
    private Locale locale;

    @Inject
    public FindGenesInMaxQuantTask(@Assisted Organism organism, MaxQuantService maxQuantService,
            @Assisted Collection<File> proteinGroupsFiles, @Assisted Locale locale) {
        this.organism = organism;
        this.maxQuantService = maxQuantService;
        this.proteinGroupsFiles = proteinGroupsFiles;
        this.locale = locale;
        bundle = ResourceBundle.getBundle(FindGenesInMaxQuantTask.class.getName(), locale);
    }

    @Override
    protected Map<File, File> call() throws Exception {
        ProgressBar progressBar = new TaskProgessBar();
        Map<File, File> results = new HashMap<File, File>();
        logger.debug("fillGeneDatabase {}", organism);
        double step = 0.4 / Math.max(proteinGroupsFiles.size(), 1);
        for (File proteinGroups : proteinGroupsFiles) {
            logger.debug("findGeneNames {}", proteinGroups);
            File withGenes = maxQuantService.findGeneNames(organism, proteinGroups, progressBar.step(step), locale);
            String filename = MessageFormat.format(bundle.getString("output.filename"),
                    FilenameUtils.getBaseName(proteinGroups.getName()));
            File output = new File(proteinGroups.getParentFile(), filename);
            logger.debug("move {} to {}", withGenes, output);
            FileUtils.moveFile(withGenes, output);
            results.put(proteinGroups, output);
        }
        logger.debug("completed files {}", proteinGroupsFiles);
        return results;
    }
}
