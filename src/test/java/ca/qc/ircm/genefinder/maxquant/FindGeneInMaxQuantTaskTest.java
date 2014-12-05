package ca.qc.ircm.genefinder.maxquant;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.Label;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.loadui.testfx.GuiTest;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import ca.qc.ircm.genefinder.maxquant.FindGenesInMaxQuantTask;
import ca.qc.ircm.genefinder.maxquant.MaxQuantService;
import ca.qc.ircm.genefinder.organism.Organism;
import ca.qc.ircm.genefinder.test.config.RetryOnFail;
import ca.qc.ircm.genefinder.test.config.TestLoggingRunner;
import ca.qc.ircm.progress_bar.ProgressBar;

/**
 * Tests for FindGeneInMaxQuantTask.
 */
@RunWith(TestLoggingRunner.class)
public class FindGeneInMaxQuantTaskTest extends GuiTest {
    private FindGenesInMaxQuantTask findGeneInMaxQuantTask;
    @Mock
    private Organism organism;
    @Mock
    private MaxQuantService maxQuantService;
    @Mock
    private ChangeListener<String> messageChangeListener;
    @Mock
    private ChangeListener<Number> progressChangeListener;
    @Mock
    private EventHandler<WorkerStateEvent> cancelHandler;
    @Captor
    private ArgumentCaptor<ObservableValue<String>> observableMessageCaptor;
    @Captor
    private ArgumentCaptor<ObservableValue<Number>> observableProgressCaptor;
    private List<File> proteinGroupsFiles = new ArrayList<File>();
    private Map<File, File> maxQuantServiceOutputs = new HashMap<File, File>();
    private Locale locale;
    private Random random = new Random();
    private Map<File, Long> expectedChecksums = new HashMap<File, Long>();
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Override
    protected Parent getRootNode() {
        return new Label("test");
    }

    @Before
    public void beforeTest() throws Throwable {
        proteinGroupsFiles.add(temporaryFolder.newFile("proteinGroups1.txt"));
        proteinGroupsFiles.add(temporaryFolder.newFile("proteinGroups2.txt"));
        locale = Locale.getDefault();
        findGeneInMaxQuantTask = new FindGenesInMaxQuantTask(organism, maxQuantService, proteinGroupsFiles, locale);
        maxQuantServiceOutputs.put(proteinGroupsFiles.get(0), temporaryFolder.newFile("maxQuantServiceOutput1.txt"));
        maxQuantServiceOutputs.put(proteinGroupsFiles.get(1), temporaryFolder.newFile("maxQuantServiceOutput2.txt"));
        proteinGroupsFiles.forEach(file -> {
            File maxQuantServiceOutput = maxQuantServiceOutputs.get(file);
            try {
                writeRandomData(maxQuantServiceOutput);
                expectedChecksums.put(file, FileUtils.checksumCRC32(maxQuantServiceOutput));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        when(
                maxQuantService.findGeneNames(any(Organism.class), any(File.class), any(ProgressBar.class),
                        any(Locale.class))).thenAnswer(new Answer<File>() {
            @Override
            public File answer(InvocationOnMock invocation) throws Throwable {
                File file = (File) invocation.getArguments()[1];
                ProgressBar progressBar = (ProgressBar) invocation.getArguments()[2];
                if (progressBar != null) {
                    progressBar.setMessage("fillGeneDatabase");
                    progressBar.setProgress(1.0);
                }
                return maxQuantServiceOutputs.get(file);
            }
        });
    }

    private void writeRandomData(File file) throws IOException {
        byte[] bytes = new byte[4096];
        random.nextBytes(bytes);
        try (OutputStream output = new FileOutputStream(file)) {
            output.write(bytes);
        }
    }

    @Test
    public void call() throws Throwable {
        findGeneInMaxQuantTask.messageProperty().addListener(messageChangeListener);
        findGeneInMaxQuantTask.progressProperty().addListener(progressChangeListener);

        Map<File, File> outputs = findGeneInMaxQuantTask.call();

        File proteinGroups = proteinGroupsFiles.get(0);
        verify(maxQuantService).findGeneNames(eq(organism), eq(proteinGroups), any(ProgressBar.class), eq(locale));
        assertEquals(new File(temporaryFolder.getRoot(), "proteinGroups1WithGene.txt"), outputs.get(proteinGroups));
        assertEquals(expectedChecksums.get(proteinGroups), (Long) FileUtils.checksumCRC32(outputs.get(proteinGroups)));
        proteinGroups = proteinGroupsFiles.get(1);
        verify(maxQuantService).findGeneNames(eq(organism), eq(proteinGroups), any(ProgressBar.class), eq(locale));
        assertEquals(new File(temporaryFolder.getRoot(), "proteinGroups2WithGene.txt"), outputs.get(proteinGroups));
        assertEquals(expectedChecksums.get(proteinGroups), (Long) FileUtils.checksumCRC32(outputs.get(proteinGroups)));
        verify(messageChangeListener, atLeastOnce()).changed(observableMessageCaptor.capture(), any(String.class),
                any(String.class));
        verify(progressChangeListener, atLeastOnce()).changed(observableProgressCaptor.capture(), any(Number.class),
                any(Number.class));
    }

    @Test
    @RetryOnFail(5)
    public void cancel() throws Throwable {
        findGeneInMaxQuantTask.setOnCancelled(cancelHandler);
        when(
                maxQuantService.findGeneNames(any(Organism.class), any(File.class), any(ProgressBar.class),
                        any(Locale.class))).thenAnswer(new Answer<File>() {
            @Override
            public File answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(5000);
                File file = (File) invocation.getArguments()[1];
                return maxQuantServiceOutputs.get(file);
            }
        });
        new Thread(findGeneInMaxQuantTask).start();

        findGeneInMaxQuantTask.cancel();

        verify(cancelHandler).handle(any(WorkerStateEvent.class));
    }
}
