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

import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Collection;
import java.util.Locale;

import javax.inject.Inject;

/**
 * Creates instances of {@link FindGenesInDataTask}.
 */
@Component
public class FindGenesInDataTaskFactory {
  @Inject
  private DataService dataService;

  public FindGenesInDataTask create(Collection<File> files, FindGenesParameters findGenesParameter,
      Locale locale) {
    return new FindGenesInDataTask(dataService, files, findGenesParameter, locale);
  }
}
