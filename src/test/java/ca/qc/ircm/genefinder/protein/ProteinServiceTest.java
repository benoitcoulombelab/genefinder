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

package ca.qc.ircm.genefinder.protein;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.qc.ircm.genefinder.test.config.ServiceTestAnnotations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@ServiceTestAnnotations
public class ProteinServiceTest {
  private ProteinService proteinServiceDefault;

  @BeforeEach
  public void beforeTest() throws Throwable {
    proteinServiceDefault = new ProteinService();
  }

  @Test
  public void weight() {
    double weight = proteinServiceDefault.weight("GAMPSTRV");

    assertEquals(0.82, weight, 0.01);
  }

  @Test
  public void weight_Lowercase() {
    double weight = proteinServiceDefault.weight("mpstyllq");

    assertEquals(0.95, weight, 0.01);
  }

  @Test
  public void weight_Invalid() {
    double weight = proteinServiceDefault.weight("mpstyllq574");

    assertEquals(0.95, weight, 0.01);
  }

  @Test
  public void weight_Empty() {
    double weight = proteinServiceDefault.weight("");

    assertEquals(0.0, weight, 0.01);
  }
}
