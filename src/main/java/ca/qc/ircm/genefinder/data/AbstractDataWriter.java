package ca.qc.ircm.genefinder.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;

public abstract class AbstractDataWriter implements DataWriter {
  private String getProteinIdFromMatcher(Matcher matcher) {
    String proteinId = null;
    int index = 1;
    while (proteinId == null && index <= matcher.groupCount()) {
      proteinId = matcher.group(index++);
    }
    return proteinId;
  }

  protected List<String> parseProteinIds(String content) throws IOException {
    String[] rawProteins = content.split(";", -1);
    Set<String> proteinIds = new LinkedHashSet<>();
    for (String rawProtein : rawProteins) {
      Matcher matcher = PROTEIN_PATTERN.matcher(rawProtein);
      if (matcher.find()) {
        String protein = getProteinIdFromMatcher(matcher);
        proteinIds.add(protein);
      }
    }
    return new ArrayList<>(proteinIds);
  }

  protected <E> String formatCollection(Collection<E> elements, Function<E, String> toString) {
    StringBuilder builder = new StringBuilder();
    elements.forEach(e -> {
      builder.append(toString.apply(e));
      builder.append(";");
    });
    if (builder.length() > 0) {
      builder.deleteCharAt(builder.length() - 1);
    }
    return builder.toString();
  }
}
