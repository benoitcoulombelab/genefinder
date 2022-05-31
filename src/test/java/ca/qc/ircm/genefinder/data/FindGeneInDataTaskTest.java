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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import ca.qc.ircm.genefinder.test.config.RetryOnFail;
import ca.qc.ircm.genefinder.test.config.RetryOnFailExtension;
import ca.qc.ircm.genefinder.test.config.TestFxTestAnnotations;
import ca.qc.ircm.progressbar.ProgressBar;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

@TestFxTestAnnotations
@ExtendWith({ RetryOnFailExtension.class, ApplicationExtension.class })
public class FindGeneInDataTaskTest {
  private FindGenesInDataTask findGenesInDataTask;
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
  @TempDir
  File temporaryFolder;

  @Start
  public void start(Stage stage) throws Exception {
  }

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() throws Throwable {
    dataFiles.add(new File(temporaryFolder, "data1.txt"));
    dataFiles.add(new File(temporaryFolder, "data2.txt"));
    locale = Locale.getDefault();
    findGenesInDataTask = new FindGenesInDataTask(dataService, dataFiles, parameters, locale);
    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        ProgressBar progressBar = (ProgressBar) invocation.getArguments()[2];
        if (progressBar != null) {
          progressBar.setMessage("fillGeneDatabase");
          progressBar.setProgress(1.0);
        }
        return null;
      }
    }).when(dataService).findGeneNames(any(), any(), any(), any(Locale.class));
  }

  @Test
  public void call(FxRobot robot) throws Throwable {
    findGenesInDataTask.messageProperty().addListener(messageChangeListener);
    findGenesInDataTask.progressProperty().addListener(progressChangeListener);

    findGenesInDataTask.call();

    verify(dataService).findGeneNames(eq(dataFiles), eq(parameters), any(ProgressBar.class),
        eq(locale));
    verify(messageChangeListener, atLeastOnce()).changed(observableMessageCaptor.capture(),
        any(String.class), any(String.class));
    verify(progressChangeListener, atLeastOnce()).changed(observableProgressCaptor.capture(),
        any(Number.class), any(Number.class));
  }

  @Test
  @RetryOnFail(5)
  public void cancel(FxRobot robot) throws Throwable {
    findGenesInDataTask.setOnCancelled(cancelHandler);

    new Thread(() -> findGenesInDataTask.cancel()).start();

    Thread.sleep(1000);
    verify(cancelHandler).handle(any(WorkerStateEvent.class));
  }
}
