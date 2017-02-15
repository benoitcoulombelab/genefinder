package ca.qc.ircm.genefinder.data;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import ca.qc.ircm.genefinder.organism.Organism;
import ca.qc.ircm.genefinder.test.config.RetryOnFail;
import ca.qc.ircm.genefinder.test.config.RetryOnFailRule;
import ca.qc.ircm.genefinder.test.config.TestFxTestAnnotations;
import ca.qc.ircm.progressbar.ProgressBar;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testfx.framework.junit.ApplicationTest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RunWith(SpringJUnit4ClassRunner.class)
@TestFxTestAnnotations
public class FindGeneInDataTaskTest extends ApplicationTest {
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
  private List<File> dataFiles = new ArrayList<>();
  private Locale locale;
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  public RetryOnFailRule retryOnFailRule = new RetryOnFailRule();
  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(retryOnFailRule).around(temporaryFolder);

  @Override
  public void start(Stage stage) throws Exception {
  }

  /**
   * Before test.
   */
  @Before
  public void beforeTest() throws Throwable {
    dataFiles.add(temporaryFolder.newFile("data1.txt"));
    dataFiles.add(temporaryFolder.newFile("data2.txt"));
    locale = Locale.getDefault();
    findGenesInDataTask =
        new FindGenesInDataTask(organism, dataService, dataFiles, parameters, locale);
    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        ProgressBar progressBar = (ProgressBar) invocation.getArguments()[3];
        if (progressBar != null) {
          progressBar.setMessage("fillGeneDatabase");
          progressBar.setProgress(1.0);
        }
        return null;
      }
    }).when(dataService).findGeneNames(any(), any(), any(), any(), any(Locale.class));
  }

  @Test
  public void call() throws Throwable {
    findGenesInDataTask.messageProperty().addListener(messageChangeListener);
    findGenesInDataTask.progressProperty().addListener(progressChangeListener);

    findGenesInDataTask.call();

    verify(dataService).findGeneNames(eq(organism), eq(dataFiles), eq(parameters),
        any(ProgressBar.class), eq(locale));
    verify(messageChangeListener, atLeastOnce()).changed(observableMessageCaptor.capture(),
        any(String.class), any(String.class));
    verify(progressChangeListener, atLeastOnce()).changed(observableProgressCaptor.capture(),
        any(Number.class), any(Number.class));
  }

  @Test
  @RetryOnFail(5)
  public void cancel() throws Throwable {
    findGenesInDataTask.setOnCancelled(cancelHandler);

    new Thread(() -> findGenesInDataTask.cancel()).start();

    Thread.sleep(1000);
    verify(cancelHandler).handle(any(WorkerStateEvent.class));
  }
}
