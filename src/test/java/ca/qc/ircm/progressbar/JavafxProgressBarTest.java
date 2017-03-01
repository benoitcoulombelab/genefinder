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

package ca.qc.ircm.progressbar;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class JavafxProgressBarTest {
  @Mock
  private JavafxProgressBar mockedProgressBar;
  @Captor
  private ArgumentCaptor<Double> progressCaptor;

  @Before
  public void beforeTest() {
    MockitoAnnotations.initMocks(this);
  }

  @After
  public void afterTest() {
    Mockito.validateMockitoUsage();
  }

  @Test
  public void progress() {
    JavafxProgressBar progressBar = new JavafxProgressBar();
    progressBar.setProgress(0);
    assertEquals(0.0, progressBar.getProgress(), 0.0001);
    progressBar.setProgress(0.546);
    assertEquals(0.546, progressBar.getProgress(), 0.0001);
    progressBar.setProgress(1);
    assertEquals(1.0, progressBar.getProgress(), 0.0001);
  }

  @Test
  public void progress_TooLow() {
    JavafxProgressBar progressBar = new JavafxProgressBar();
    progressBar.setProgress(-0.1);
    assertEquals(0.0, progressBar.getProgress(), 0.0001);
  }

  @Test
  public void progress_TooHigh() {
    JavafxProgressBar progressBar = new JavafxProgressBar();
    progressBar.setProgress(1.2);
    assertEquals(1.0, progressBar.getProgress(), 0.0001);
  }

  @Test
  public void changeProgress() {
    JavafxProgressBar mainProgressBar = new JavafxProgressBar();
    mainProgressBar.progress().addListener(
        (observable, old, newValue) -> mockedProgressBar.setProgress(newValue.doubleValue()));

    mainProgressBar.setProgress(0.2);

    verify(mockedProgressBar).setProgress(0.2);
  }

  @Test
  public void changeProgress_Step() {
    JavafxProgressBar mainProgressBar = new JavafxProgressBar();
    mainProgressBar.progress().addListener(
        (observable, old, newValue) -> mockedProgressBar.setProgress(newValue.doubleValue()));
    ProgressBar stepProgressBar = mainProgressBar.step(0.5);

    stepProgressBar.setProgress(0.2);

    verify(mockedProgressBar).setProgress(0.1);
  }

  @Test
  public void changeProgress_MultipleSteps() {
    JavafxProgressBar mainProgressBar = new JavafxProgressBar();
    mainProgressBar.progress().addListener(
        (observable, old, newValue) -> mockedProgressBar.setProgress(newValue.doubleValue()));
    ProgressBar stepProgressBar = mainProgressBar.step(0.5);
    ProgressBar subStepProgressBar = stepProgressBar.step(0.5);

    subStepProgressBar.setProgress(0.5);

    verify(mockedProgressBar).setProgress(0.125);
  }

  @Test
  public void message() {
    JavafxProgressBar progressBar = new JavafxProgressBar();

    progressBar.setMessage("test");

    assertEquals("test", progressBar.getMessage());
  }

  @Test
  public void changeMessage() {
    JavafxProgressBar mainProgressBar = new JavafxProgressBar();
    mainProgressBar.message()
        .addListener((observable, old, newValue) -> mockedProgressBar.setMessage(newValue));

    mainProgressBar.setMessage("test");

    verify(mockedProgressBar).setMessage("test");
  }

  @Test
  public void changeMessage_Step() {
    JavafxProgressBar mainProgressBar = new JavafxProgressBar();
    mainProgressBar.message()
        .addListener((observable, old, newValue) -> mockedProgressBar.setMessage(newValue));
    ProgressBar stepProgressBar = mainProgressBar.step(0.5);

    stepProgressBar.setMessage("test");

    verify(mockedProgressBar).setMessage("test");
  }

  @Test
  public void changeMessage_MultipleStep() {
    JavafxProgressBar mainProgressBar = new JavafxProgressBar();
    mainProgressBar.message()
        .addListener((observable, old, newValue) -> mockedProgressBar.setMessage(newValue));
    ProgressBar stepProgressBar = mainProgressBar.step(0.5);
    ProgressBar subStepProgressBar = stepProgressBar.step(0.5);

    subStepProgressBar.setMessage("test");

    verify(mockedProgressBar).setMessage("test");
  }

  @Test
  public void title() {
    JavafxProgressBar progressBar = new JavafxProgressBar();

    progressBar.setTitle("test");

    assertEquals("test", progressBar.getTitle());
  }

  @Test
  public void changeTitle() {
    JavafxProgressBar mainProgressBar = new JavafxProgressBar();
    mainProgressBar.title()
        .addListener((observable, old, newValue) -> mockedProgressBar.setTitle(newValue));

    mainProgressBar.setTitle("test");

    verify(mockedProgressBar).setTitle("test");
  }

  @Test
  public void changeTitle_Step() {
    JavafxProgressBar mainProgressBar = new JavafxProgressBar();
    mainProgressBar.title()
        .addListener((observable, old, newValue) -> mockedProgressBar.setTitle(newValue));
    ProgressBar stepProgressBar = mainProgressBar.step(0.5);

    stepProgressBar.setTitle("test");

    verify(mockedProgressBar).setTitle("test");
  }

  @Test
  public void changeTitle_MultipleStep() {
    JavafxProgressBar mainProgressBar = new JavafxProgressBar();
    mainProgressBar.title()
        .addListener((observable, old, newValue) -> mockedProgressBar.setTitle(newValue));
    ProgressBar stepProgressBar = mainProgressBar.step(0.5);
    ProgressBar subStepProgressBar = stepProgressBar.step(0.5);

    subStepProgressBar.setTitle("test");

    verify(mockedProgressBar).setTitle("test");
  }

  @Test
  public void step() {
    JavafxProgressBar progressBar = new JavafxProgressBar();
    progressBar.setProgress(0.3);
    ProgressBar stepProgressBar = progressBar.step(0.6);
    stepProgressBar.setProgress(0.2);
    assertEquals(0.42, progressBar.getProgress(), 0.0001);
    assertEquals(0.2, stepProgressBar.getProgress(), 0.0001);
    stepProgressBar.setProgress(0.0);
    assertEquals(0.3, progressBar.getProgress(), 0.0001);
    assertEquals(0.0, stepProgressBar.getProgress(), 0.0001);
    stepProgressBar.setProgress(0.5);
    assertEquals(0.6, progressBar.getProgress(), 0.0001);
    assertEquals(0.5, stepProgressBar.getProgress(), 0.0001);
    stepProgressBar.setProgress(1.0);
    assertEquals(0.9, progressBar.getProgress(), 0.0001);
    assertEquals(1.0, stepProgressBar.getProgress(), 0.0001);
  }

  @Test
  public void step_TooLow() {
    JavafxProgressBar progressBar = new JavafxProgressBar();
    progressBar.setProgress(0.3);
    ProgressBar stepProgressBar = progressBar.step(0.6);
    stepProgressBar.setProgress(-0.2);
    assertEquals(0.3, progressBar.getProgress(), 0.0001);
    assertEquals(0.0, stepProgressBar.getProgress(), 0.0001);
  }

  @Test
  public void step_TooHigh() {
    JavafxProgressBar progressBar = new JavafxProgressBar();
    progressBar.setProgress(0.3);
    ProgressBar stepProgressBar = progressBar.step(0.6);
    stepProgressBar.setProgress(1.2);
    assertEquals(0.9, progressBar.getProgress(), 0.0001);
    assertEquals(1.0, stepProgressBar.getProgress(), 0.0001);
  }

  @Test
  public void step_RecursiveStep() {
    JavafxProgressBar progressBar = new JavafxProgressBar();
    progressBar.setProgress(0.2);
    ProgressBar stepProgressBar = progressBar.step(0.8);
    stepProgressBar.setProgress(0.25);
    assertEquals(0.4, progressBar.getProgress(), 0.0001);
    assertEquals(0.25, stepProgressBar.getProgress(), 0.0001);
    ProgressBar subStepProgressBar = stepProgressBar.step(0.4);
    subStepProgressBar.setProgress(0.25);
    assertEquals(0.48, progressBar.getProgress(), 0.0001);
    assertEquals(0.35, stepProgressBar.getProgress(), 0.0001);
    assertEquals(0.25, subStepProgressBar.getProgress(), 0.0001);
    subStepProgressBar.setProgress(1.0);
    assertEquals(0.72, progressBar.getProgress(), 0.0001);
    assertEquals(0.65, stepProgressBar.getProgress(), 0.0001);
    assertEquals(1.0, subStepProgressBar.getProgress(), 0.0001);
    stepProgressBar.setProgress(0.75);
    assertEquals(0.8, progressBar.getProgress(), 0.0001);
    assertEquals(0.75, stepProgressBar.getProgress(), 0.0001);
  }

  @Test
  public void message_Step() {
    JavafxProgressBar progressBar = new JavafxProgressBar();
    ProgressBar stepProgressBar = progressBar.step(0.8);

    progressBar.setMessage("overriden");
    stepProgressBar.setMessage("test");

    assertEquals("test", stepProgressBar.getMessage());
    assertEquals("test", progressBar.getMessage());
  }
}
