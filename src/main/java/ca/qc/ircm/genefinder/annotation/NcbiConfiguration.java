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
 * NCBI's configuration.
 */
public interface NcbiConfiguration {
  /**
   * Returns RefSeq's protein accession pattern.
   *
   * @return RefSeq's protein accession pattern
   */
  public Pattern refseqProteinAccessionPattern();

  /**
   * Returns RefSeq's protein GI pattern.
   *
   * @return RefSeq's protein GI pattern
   */
  public Pattern refseqProteinGiPattern();

  /**
   * Returns NCBI's EUtils URL.
   *
   * @return NCBI's EUtils URL
   */
  public String eutils();

  /**
   * Returns max ids per request on NCBI's EUtils service.
   *
   * @return max ids per request on NCBI's EUtils service
   */
  public int maxIdsPerRequest();
}
