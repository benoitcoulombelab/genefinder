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

package ca.qc.ircm.genefinder.annotation;

import java.util.regex.Pattern;

/**
 * UniProt's configuration.
 */
public interface UniprotConfiguration {
  /**
   * Returns UniProt's id mapping URL.
   *
   * @return UniProt's id mapping URL
   */
  public String mapping();

  /**
   * Returns UniProt's protein id pattern.
   *
   * @return UniProt's protein id pattern
   */
  public Pattern proteinIdPattern();

  /**
   * Returns max ids per request on UniProt REST service.
   *
   * @return max ids per request on UniProt REST service
   */
  public int maxIdsPerRequest();
}
