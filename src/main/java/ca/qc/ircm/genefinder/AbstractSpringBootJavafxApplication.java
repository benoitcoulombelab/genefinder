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

import javafx.application.Application;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Java FX application using Spring Boot.
 */
public abstract class AbstractSpringBootJavafxApplication extends Application {
  protected ConfigurableApplicationContext applicationContext;

  @Override
  public void init() throws Exception {
    applicationContext =
        SpringApplication.run(getClass(), getParameters().getRaw().toArray(new String[0]));
    applicationContext.getAutowireCapableBeanFactory().autowireBean(this);
  }

  @Override
  public void stop() throws Exception {
    super.stop();
    applicationContext.close();
  }
}
