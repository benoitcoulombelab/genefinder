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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Stack;

/**
 * Stacks elements to simplify tests for parent elements.
 */
public class StackSaxHandler extends DefaultHandler implements SaxElement {
  private final Stack<SaxElement> stack = new Stack<SaxElement>();
  private SaxElement currentElement;

  @Override
  public final void startElement(String uri, String localName, String qualifiedName,
      Attributes attributes) throws SAXException {
    currentElement = new SaxElementUsingStack(qualifiedName, attributes, stack);
    startElement(currentElement.name(), currentElement.attributes());
    stack.push(currentElement);
  }

  protected void startElement(String elementName, Attributes attributes) throws SAXException {

  }

  @Override
  public final void endElement(String uri, String localName, String qualifiedName)
      throws SAXException {
    currentElement = stack.pop();
    endElement(currentElement.name());
  }

  protected void endElement(String elementName) {

  }

  @Override
  public final boolean name(String expectedCurrent) {
    return currentElement.name(expectedCurrent);
  }

  @Override
  public final String name() {
    return currentElement.name();
  }

  protected final boolean current(String expectedCurrent) {
    return name(expectedCurrent);
  }

  protected final SaxElement current() {
    return currentElement;
  }

  @Override
  public final boolean parent(String expectedParent) {
    return currentElement.parent(expectedParent);
  }

  @Override
  public final SaxElement parent() {
    return currentElement.parent();
  }

  @Override
  public final boolean hasAncestor(String expectedAncestor) {
    return currentElement.hasAncestor(expectedAncestor);
  }

  @Override
  public final String attribute(String name) {
    return currentElement.attribute(name);
  }

  protected final boolean attribute(String name, String expectedValue) {
    return currentElement.attributes().getValue(name) != null
        && currentElement.attributes().getValue(name).equals(expectedValue);
  }

  @Override
  public final SaxElement ancestor(String ancestorName) {
    return currentElement.ancestor(ancestorName);
  }

  @Override
  public final Attributes attributes() {
    return currentElement.attributes();
  }

  @Override
  public boolean hasAttribute(String name) {
    return currentElement.hasAttribute(name);
  }
}
