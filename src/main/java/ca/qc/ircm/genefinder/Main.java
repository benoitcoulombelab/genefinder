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

package ca.qc.ircm.genefinder;

import ca.qc.ircm.genefinder.gui.ApplicationGui;
import ca.qc.ircm.util.javafx.SpringAfterburnerInstanceSupplier;
import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * JavaFX application.
 */
@SpringBootApplication
public class Main extends AbstractSpringBootJavafxApplication {
  @Override
  public void init() throws Exception {
    super.init();
    com.airhacks.afterburner.injection.Injector
        .setInstanceSupplier(new SpringAfterburnerInstanceSupplier(applicationContext));
  }

  @Override
  public void start(Stage stage) throws Exception {
    ApplicationGui app = new ApplicationGui();
    notifyPreloader(new ApplicationStarted());
    app.show();
  }

  public static void main(String[] args) {
    System.setProperty("javafx.preloader", MainPreloader.class.getName());
    Application.launch(args);
  }
}
