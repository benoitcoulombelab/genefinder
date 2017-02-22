/*
 * Copyright (c) 2006 Institut de recherches cliniques de Montreal (IRCM)
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

package ca.qc.ircm.genefinder.taxonomy;

import java.util.HashSet;
import java.util.Set;

/**
 * Organism with taxonomy data.
 */
public class Taxon {
  private int id;
  private String name;
  private String rank;
  private Taxon parent;
  private Set<Taxon> children = new HashSet<>();

  public Taxon() {
  }

  public Taxon(int id) {
    this.id = id;
  }

  public Taxon(int id, Taxon parentId) {
    this.id = id;
  }

  @Override
  public String toString() {
    return "Taxon [id=" + id + "]";
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Taxon getParent() {
    return parent;
  }

  public void setParent(Taxon parent) {
    this.parent = parent;
  }

  public Set<Taxon> getChildren() {
    return children;
  }

  public void addChild(Taxon childId) {
    children.add(childId);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getRank() {
    return rank;
  }

  public void setRank(String rank) {
    this.rank = rank;
  }
}
