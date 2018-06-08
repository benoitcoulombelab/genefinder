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

import static org.junit.Assert.assertEquals;

import ca.qc.ircm.genefinder.test.config.ServiceTestAnnotations;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.regex.Pattern;

import javax.inject.Inject;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class NcbiConfigurationTest {
  @Inject
  private NcbiConfiguration ncbiConfiguration;

  @Test
  public void defaultProperties() throws Throwable {
    assertEquals(Pattern.compile("^(?:ref\\|)?([ANYXZ]P_\\d+\\.\\d+)( \\(\\+\\d+\\))?").pattern(),
        ncbiConfiguration.refseqProteinAccessionPattern().pattern());
    assertEquals(Pattern.compile("^(?:gi\\|)?(\\d+)( \\(\\+\\d+\\))?").pattern(),
        ncbiConfiguration.refseqProteinGiPattern().pattern());
    assertEquals("https://eutils.ncbi.nlm.nih.gov/entrez/eutils", ncbiConfiguration.eutils());
    assertEquals(1000, ncbiConfiguration.maxIdsPerRequest());
  }
}
