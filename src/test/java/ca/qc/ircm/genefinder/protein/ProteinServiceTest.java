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

import static org.junit.Assert.assertEquals;

import ca.qc.ircm.genefinder.protein.ProteinService;
import ca.qc.ircm.genefinder.test.config.ServiceTestAnnotations;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class ProteinServiceTest {
  private ProteinService proteinServiceDefault;

  @Before
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
