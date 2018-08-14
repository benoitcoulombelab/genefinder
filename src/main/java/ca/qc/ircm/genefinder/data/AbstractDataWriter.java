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

import ca.qc.ircm.genefinder.annotation.NcbiConfiguration;
import ca.qc.ircm.genefinder.annotation.ProteinDatabase;
import ca.qc.ircm.genefinder.annotation.UniprotConfiguration;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;

public abstract class AbstractDataWriter implements DataWriter {
  protected static final String PROTEIN_DELIMITER = ";";
  protected static final String PROTEIN_DELIMITER_PATTERN = "\\s*[;,]\\s*";
  protected static final String LIST_DELIMITER = "|";
  @Inject
  private NcbiConfiguration ncbiConfiguration;
  @Inject
  private UniprotConfiguration uniprotConfiguration;

  protected AbstractDataWriter() {
  }

  protected AbstractDataWriter(NcbiConfiguration ncbiConfiguration,
      UniprotConfiguration uniprotConfiguration) {
    this.ncbiConfiguration = ncbiConfiguration;
    this.uniprotConfiguration = uniprotConfiguration;
  }

  private String getProteinIdFromMatcher(Matcher matcher) {
    String proteinId = null;
    int index = 1;
    while (proteinId == null && index <= matcher.groupCount()) {
      proteinId = matcher.group(index++);
    }
    return proteinId;
  }

  protected List<String> parseProteinIds(String content, Pattern proteinIdPattern)
      throws IOException {
    String[] rawProteins = content.split(PROTEIN_DELIMITER_PATTERN, -1);
    Set<String> proteinIds = new LinkedHashSet<>();
    for (String rawProtein : rawProteins) {
      Matcher matcher = proteinIdPattern.matcher(rawProtein);
      if (matcher.matches()) {
        String protein = getProteinIdFromMatcher(matcher);
        proteinIds.add(protein);
      }
    }
    return new ArrayList<>(proteinIds);
  }

  protected Pattern proteinIdPattern(FindGenesParameters parameters) {
    switch (parameters.getProteinDatabase()) {
      case REFSEQ:
        return ncbiConfiguration.refseqProteinAccessionPattern();
      case REFSEQ_GI:
        return ncbiConfiguration.refseqProteinGiPattern();
      case UNIPROT:
        return uniprotConfiguration.proteinIdPattern();
      default:
        throw new AssertionError(ProteinDatabase.class.getSimpleName() + " "
            + parameters.getProteinColumn() + " not covered in switch");
    }
  }
}
