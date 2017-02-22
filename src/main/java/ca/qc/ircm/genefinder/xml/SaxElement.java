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

package ca.qc.ircm.genefinder.xml;

import org.xml.sax.Attributes;

import javax.xml.parsers.SAXParser;

/**
 * An element parsed by a {@link SAXParser}.
 */
public interface SaxElement {
  /**
   * Returns element's name.
   * 
   * @return element's name
   */
  public String name();

  /**
   * Returns true if element's name is equal to expectedName, false otherwise.
   * 
   * @param expectedName
   *          expected element's name
   * @return true if element's name is equal to expectedName, false otherwise
   */
  public boolean name(String expectedName);

  /**
   * Returns all attributes of element.
   * 
   * @return all attributes of element
   */
  public Attributes attributes();

  /**
   * Returns value of specified attribute or null if attribute is not present in element.
   * 
   * @param name
   *          attribute's name
   * @return value of specified attribute or null if attribute is not present in element
   */
  public String attribute(String name);

  /**
   * Returns true if element has an attribute with specified name, false otherwise.
   * 
   * @param name
   *          attribute's name
   * @return true if element has an attribute with specified name, false otherwise
   */
  public boolean hasAttribute(String name);

  /**
   * Returns parent element of this element.
   * 
   * @return parent element of this element
   */
  public SaxElement parent();

  /**
   * Returns true if parent element's name is equal to expectedParent, false otherwise.
   * 
   * @param expectedParent
   *          expected parent element's name
   * @return true if parent element's name is equal to expectedParent, false otherwise
   */
  public boolean parent(String expectedParent);

  /**
   * Returns the closest ancestor of element that have specified name, or null if element doesn't
   * have an ancestor with that name.
   * 
   * @param name
   *          ancestor's name
   * @return the closest ancestor of element that have specified name, or null if element doesn't
   *         have an ancestor with that name
   */
  public SaxElement ancestor(String name);

  /**
   * Returns true if element has an ancestor element with specified name, false otherwise.
   * 
   * @param name
   *          ancestor's name
   * @return true if element has an ancestor element with specified name, false otherwise
   */
  public boolean hasAncestor(String name);
}
