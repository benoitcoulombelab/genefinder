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
    String[] rawProteins = content.split(PROTEIN_DELIMITER, -1);
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
      case SWISSPROT:
        return uniprotConfiguration.proteinIdPattern();
      default:
        throw new AssertionError(ProteinDatabase.class.getSimpleName() + " "
            + parameters.getProteinColumn() + " not covered in switch");
    }
  }
}
