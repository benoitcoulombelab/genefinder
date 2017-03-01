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

/**
 * Progress bar.
 */
public interface ProgressBar {
  /**
   * Returns the current progress.
   *
   * @return the current progress
   */
  public double getProgress();

  /**
   * Sets the current progress.
   *
   * @param progress
   *          current progress between 0 and 1
   */
  public void setProgress(double progress);

  /**
   * Returns progress title.
   *
   * @return progress title
   */
  public String getTitle();

  /**
   * Sets progress title.
   *
   * @param title
   *          progress title
   */
  public void setTitle(String title);

  /**
   * Returns progress message.
   *
   * @return progress message
   */
  public String getMessage();

  /**
   * Sets progress message.
   *
   * @param message
   *          progress message
   */
  public void setMessage(String message);

  /**
   * Creates a {@link ProgressBar} to use in a sub process that records progression from 0 to 1.
   * <br>
   * For example, if this progress bar's current value is 0.3, calling
   * <code>progressBar.step(0.2).setProgress(0.5);</code> will change progress to 0.4.
   *
   * <p>
   * This method and the returned {@link ProgressBar} may not be thread safe depending on the
   * implementation.
   * </p>
   *
   * <p>
   * Setting message on the returned {@link ProgressBar} will change the message in this progress
   * bar (the parent progress bar).
   * </p>
   *
   * @param step
   *          percentage of progress that the step should represent (between 0 and 1)
   * @return {@link ProgressBar} to use in a sub process
   */
  public ProgressBar step(double step);
}
