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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;
import org.junit.jupiter.api.Test;

public class ProteinDatabaseTest {
  @Test
  public void getLabel_Refseq() {
    String label = ProteinDatabase.REFSEQ.getLabel(Locale.CANADA);

    assertEquals("RefSeq", label);
  }

  @Test
  public void getLabel_RefseqGi() {
    String label = ProteinDatabase.REFSEQ_GI.getLabel(Locale.CANADA);

    assertEquals("RefSeq (gi numbers)", label);
  }

  @Test
  public void getLabel_Uniprot() {
    String label = ProteinDatabase.UNIPROT.getLabel(Locale.CANADA);

    assertEquals("UniProt", label);
  }
}
