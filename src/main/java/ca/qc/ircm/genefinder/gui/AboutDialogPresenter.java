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

package ca.qc.ircm.genefinder.gui;

import java.io.IOException;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * About dialog presenter.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AboutDialogPresenter {
  private static final String FLATICON_URL = "https://www.flaticon.com";
  private static final String ICONS8_URL = "https://icons8.com";
  private static final Logger logger = LoggerFactory.getLogger(AboutDialogPresenter.class);
  @FXML
  private Button ok;

  @FXML
  private void initialize() {
    ok.requestFocus();
  }

  private void openLink(String url) {
    try {
      BrowserOpener.open(url);
    } catch (IOException e) {
      logger.debug("could not open browser for URL {}", url, e);
    }
  }

  @FXML
  private void openFlaticon() {
    openLink(FLATICON_URL);
  }

  @FXML
  private void openIcons8() {
    openLink(ICONS8_URL);
  }

  @FXML
  private void close(Event event) {
    ok.getScene().getWindow().hide();
  }

  void focusOnDefault() {
    ok.requestFocus();
  }
}
