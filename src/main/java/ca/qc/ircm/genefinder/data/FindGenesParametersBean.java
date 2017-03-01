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

import ca.qc.ircm.genefinder.annotation.ProteinDatabase;

/**
 * Parameters for finding genes in data files.
 */
public class FindGenesParametersBean implements FindGenesParameters {
  private ProteinDatabase proteinDatabase;
  private int proteinColumn;
  private boolean geneId;
  private boolean geneName;
  private boolean geneSynonyms;
  private boolean geneSummary;
  private boolean proteinMolecularWeight;
  private boolean sequence;

  @Override
  public ProteinDatabase getProteinDatabase() {
    return proteinDatabase;
  }

  public FindGenesParametersBean proteinDatabase(ProteinDatabase proteinDatabase) {
    this.proteinDatabase = proteinDatabase;
    return this;
  }

  @Override
  public int getProteinColumn() {
    return proteinColumn;
  }

  public FindGenesParametersBean proteinColumn(int proteinColumn) {
    this.proteinColumn = proteinColumn;
    return this;
  }

  @Override
  public boolean isGeneId() {
    return geneId;
  }

  public FindGenesParametersBean geneId(boolean geneId) {
    this.geneId = geneId;
    return this;
  }

  @Override
  public boolean isGeneName() {
    return geneName;
  }

  public FindGenesParametersBean geneName(boolean geneName) {
    this.geneName = geneName;
    return this;
  }

  @Override
  public boolean isGeneSynonyms() {
    return geneSynonyms;
  }

  public FindGenesParametersBean geneSynonyms(boolean geneSynonyms) {
    this.geneSynonyms = geneSynonyms;
    return this;
  }

  @Override
  public boolean isGeneSummary() {
    return geneSummary;
  }

  public FindGenesParametersBean geneSummary(boolean geneSummary) {
    this.geneSummary = geneSummary;
    return this;
  }

  @Override
  public boolean isProteinMolecularWeight() {
    return proteinMolecularWeight;
  }

  public FindGenesParametersBean proteinMolecularWeight(boolean proteinMolecularWeight) {
    this.proteinMolecularWeight = proteinMolecularWeight;
    return this;
  }

  @Override
  public boolean isSequence() {
    return sequence;
  }

  public FindGenesParametersBean sequence(boolean sequence) {
    this.sequence = sequence;
    return this;
  }
}
