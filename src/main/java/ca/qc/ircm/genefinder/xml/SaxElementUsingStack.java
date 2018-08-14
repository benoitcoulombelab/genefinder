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

package ca.qc.ircm.genefinder.xml;

import java.util.ListIterator;
import java.util.Stack;
import org.xml.sax.Attributes;

/**
 * {@link SaxElement} containing its parent inside a {@link Stack}.
 */
public class SaxElementUsingStack implements SaxElement {
  private final String name;
  private final Attributes attributes;
  private final Stack<SaxElement> parents;

  /**
   * Creates sax element containing its parent inside a stack.
   * 
   * @param name
   *          element's name
   * @param attributes
   *          element's attributes
   * @param parents
   *          element's parents
   */
  @SuppressWarnings("unchecked")
  public SaxElementUsingStack(String name, Attributes attributes, Stack<SaxElement> parents) {
    this.name = name;
    this.attributes = attributes;
    this.parents = (Stack<SaxElement>) parents.clone();
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public boolean name(String expectedName) {
    return name.equals(expectedName);
  }

  @Override
  public Attributes attributes() {
    return attributes;
  }

  @Override
  public String attribute(String name) {
    return attributes.getValue(name);
  }

  @Override
  public boolean hasAttribute(String name) {
    return attributes.getValue(name) != null;
  }

  @Override
  public SaxElement parent() {
    if (!parents.isEmpty()) {
      return parents.peek();
    } else {
      return null;
    }
  }

  @Override
  public boolean parent(String expectedParent) {
    SaxElement parent = parent();
    return parent != null && parent.name(expectedParent);
  }

  @Override
  public SaxElement ancestor(String name) {
    ListIterator<SaxElement> elements = parents.listIterator(parents.size());
    while (elements.hasPrevious()) {
      SaxElement element = elements.previous();
      if (element.name().equals(name)) {
        return element;
      }
    }
    return null;
  }

  @Override
  public boolean hasAncestor(String name) {
    return ancestor(name) != null;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("SaxElement");
    builder.append("_");
    builder.append(name);
    return builder.toString();
  }
}
