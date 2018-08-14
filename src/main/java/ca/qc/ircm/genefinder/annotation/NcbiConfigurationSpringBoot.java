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
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = NcbiConfigurationSpringBoot.PREFIX)
public class NcbiConfigurationSpringBoot implements NcbiConfiguration {
  public static final String PREFIX = "ncbi";
  private String refseqProteinAccessionPattern;
  private String refseqProteinGiPattern;
  private String eutils;
  private int maxIdsPerRequest;

  @Override
  public Pattern refseqProteinAccessionPattern() {
    return Pattern.compile(refseqProteinAccessionPattern);
  }

  @Override
  public Pattern refseqProteinGiPattern() {
    return Pattern.compile(refseqProteinGiPattern);
  }

  @Override
  public String eutils() {
    return eutils;
  }

  @Override
  public int maxIdsPerRequest() {
    return maxIdsPerRequest;
  }

  public String getEutils() {
    return eutils;
  }

  public void setEutils(String eutils) {
    this.eutils = eutils;
  }

  public int getMaxIdsPerRequest() {
    return maxIdsPerRequest;
  }

  public void setMaxIdsPerRequest(int maxIdsPerRequest) {
    this.maxIdsPerRequest = maxIdsPerRequest;
  }

  public String getRefseqProteinAccessionPattern() {
    return refseqProteinAccessionPattern;
  }

  public void setRefseqProteinAccessionPattern(String refseqProteinAccessionPattern) {
    this.refseqProteinAccessionPattern = refseqProteinAccessionPattern;
  }

  public String getRefseqProteinGiPattern() {
    return refseqProteinGiPattern;
  }

  public void setRefseqProteinGiPattern(String refseqProteinGiPattern) {
    this.refseqProteinGiPattern = refseqProteinGiPattern;
  }
}
