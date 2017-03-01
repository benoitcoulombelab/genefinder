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

package ca.qc.ircm.genefinder.data.gui;

import static ca.qc.ircm.genefinder.annotation.ProteinDatabase.REFSEQ;
import static ca.qc.ircm.genefinder.annotation.ProteinDatabase.REFSEQ_GI;
import static ca.qc.ircm.genefinder.annotation.ProteinDatabase.UNIPROT;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Locale;

public class ProteinDatabaseStringConverterTest {
  private Locale locale = Locale.getDefault();
  private ProteinDatabaseStringConverter proteinDatabaseStringConverter =
      new ProteinDatabaseStringConverter(locale);

  @Test
  public void fromString_Refseq() {
    assertEquals(REFSEQ, proteinDatabaseStringConverter.fromString(REFSEQ.getLabel(locale)));
  }

  @Test
  public void fromString_RefseqGi() {
    assertEquals(REFSEQ_GI, proteinDatabaseStringConverter.fromString(REFSEQ_GI.getLabel(locale)));
  }

  @Test
  public void fromString_Uniprot() {
    assertEquals(UNIPROT, proteinDatabaseStringConverter.fromString(UNIPROT.getLabel(locale)));
  }

  @Test
  public void toString_Refseq() {
    assertEquals(REFSEQ.getLabel(locale), proteinDatabaseStringConverter.toString(REFSEQ));
  }

  @Test
  public void toString_RefseqGi() {
    assertEquals(REFSEQ_GI.getLabel(locale), proteinDatabaseStringConverter.toString(REFSEQ_GI));
  }

  @Test
  public void toString_Uniprot() {
    assertEquals(UNIPROT.getLabel(locale), proteinDatabaseStringConverter.toString(UNIPROT));
  }
}
