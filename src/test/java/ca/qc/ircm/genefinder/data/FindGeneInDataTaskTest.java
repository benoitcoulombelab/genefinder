package ca.qc.ircm.genefinder.data;

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

import ca.qc.ircm.genefinder.organism.Organism;
import ca.qc.ircm.genefinder.test.config.RetryOnFail;
import ca.qc.ircm.genefinder.test.config.TestLoggingRunner;
import ca.qc.ircm.progress_bar.ProgressBar;

@RunWith(TestLoggingRunner.class)
public class FindGeneInDataTaskTest extends GuiTest {
    private FindGenesInDataTask findGenesInDataTask;
    @Mock
    private Organism organism;
    @Mock
    private DataService dataService;
    @Mock
    private FindGenesParameters parameters;
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
    private List<File> dataFiles = new ArrayList<File>();
    private Map<File, File> dataServiceOutputs = new HashMap<File, File>();
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
        dataFiles.add(temporaryFolder.newFile("data1.txt"));
        dataFiles.add(temporaryFolder.newFile("data2.txt"));
        locale = Locale.getDefault();
        findGenesInDataTask = new FindGenesInDataTask(organism, dataService, dataFiles, parameters, locale);
        dataServiceOutputs.put(dataFiles.get(0), temporaryFolder.newFile("dataServiceOutput1.txt"));
        dataServiceOutputs.put(dataFiles.get(1), temporaryFolder.newFile("dataServiceOutput2.txt"));
        dataFiles.forEach(file -> {
            File maxQuantServiceOutput = dataServiceOutputs.get(file);
            try {
                writeRandomData(maxQuantServiceOutput);
                expectedChecksums.put(file, FileUtils.checksumCRC32(maxQuantServiceOutput));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        when(dataService.findGeneNames(any(), any(), any(), any(), any(Locale.class))).thenAnswer(new Answer<File>() {
            @Override
            public File answer(InvocationOnMock invocation) throws Throwable {
                File file = (File) invocation.getArguments()[1];
                ProgressBar progressBar = (ProgressBar) invocation.getArguments()[3];
                if (progressBar != null) {
                    progressBar.setMessage("fillGeneDatabase");
                    progressBar.setProgress(1.0);
                }
                return dataServiceOutputs.get(file);
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
        findGenesInDataTask.messageProperty().addListener(messageChangeListener);
        findGenesInDataTask.progressProperty().addListener(progressChangeListener);

        Map<File, File> outputs = findGenesInDataTask.call();

        File proteinGroups = dataFiles.get(0);
        verify(dataService).findGeneNames(eq(organism), eq(proteinGroups), eq(parameters), any(ProgressBar.class),
                eq(locale));
        assertEquals(new File(temporaryFolder.getRoot(), "data1WithGene.txt"), outputs.get(proteinGroups));
        assertEquals(expectedChecksums.get(proteinGroups), (Long) FileUtils.checksumCRC32(outputs.get(proteinGroups)));
        proteinGroups = dataFiles.get(1);
        verify(dataService).findGeneNames(eq(organism), eq(proteinGroups), eq(parameters), any(ProgressBar.class),
                eq(locale));
        assertEquals(new File(temporaryFolder.getRoot(), "data2WithGene.txt"), outputs.get(proteinGroups));
        assertEquals(expectedChecksums.get(proteinGroups), (Long) FileUtils.checksumCRC32(outputs.get(proteinGroups)));
        verify(messageChangeListener, atLeastOnce()).changed(observableMessageCaptor.capture(), any(String.class),
                any(String.class));
        verify(progressChangeListener, atLeastOnce()).changed(observableProgressCaptor.capture(), any(Number.class),
                any(Number.class));
    }

    @Test
    @RetryOnFail(5)
    public void cancel() throws Throwable {
        findGenesInDataTask.setOnCancelled(cancelHandler);
        when(dataService.findGeneNames(any(), any(), any(), any(), any())).thenAnswer(new Answer<File>() {
            @Override
            public File answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(5000);
                File file = (File) invocation.getArguments()[1];
                return dataServiceOutputs.get(file);
            }
        });
        new Thread(findGenesInDataTask).start();

        findGenesInDataTask.cancel();

        verify(cancelHandler).handle(any(WorkerStateEvent.class));
    }
}
