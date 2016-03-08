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
  protected class Header {
    protected Integer giColumnIndex;
  }

  protected List<Integer> parseGis(String content) throws IOException {
    Set<Integer> gis = new LinkedHashSet<>();
    int start = 0;
    Matcher matcher = GI_PATTERN.matcher(content);
    while (matcher.find(start)) {
      gis.add(Integer.parseInt(matcher.group(1)));
      start = matcher.end();
    }
    return new ArrayList<>(gis);
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

  protected boolean finishedHeader(Header header) {
    return header.giColumnIndex != null;
  }
}
