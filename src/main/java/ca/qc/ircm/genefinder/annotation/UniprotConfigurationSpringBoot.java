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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.regex.Pattern;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = UniprotConfigurationSpringBoot.PREFIX)
public class UniprotConfigurationSpringBoot implements UniprotConfiguration {
  public static final String PREFIX = "uniprot";
  private String mapping;
  private String proteinIdPattern;
  private int maxIdsPerRequest;

  @Override
  public String mapping() {
    return mapping;
  }

  @Override
  public Pattern proteinIdPattern() {
    return Pattern.compile(proteinIdPattern);
  }

  @Override
  public int maxIdsPerRequest() {
    return maxIdsPerRequest;
  }

  public String getProteinIdPattern() {
    return proteinIdPattern;
  }

  public void setProteinIdPattern(String proteinIdPattern) {
    this.proteinIdPattern = proteinIdPattern;
  }

  public int getMaxIdsPerRequest() {
    return maxIdsPerRequest;
  }

  public void setMaxIdsPerRequest(int maxIdsPerRequest) {
    this.maxIdsPerRequest = maxIdsPerRequest;
  }

  public String getMapping() {
    return mapping;
  }

  public void setMapping(String mapping) {
    this.mapping = mapping;
  }
}
