package ca.qc.ircm.genefinder.annotation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import ca.qc.ircm.genefinder.test.config.Rules;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class IdMappingParserTest {
  private IdMappingParser idMappingParser;
  @Rule
  public RuleChain rules = Rules.defaultRules(this);
  @Mock
  private Consumer<IdMapping> handler;
  @Captor
  private ArgumentCaptor<IdMapping> idMappingCaptor;

  @Before
  public void beforeTest() {
    idMappingParser = new IdMappingParser();
  }

  private IdMapping find(Collection<IdMapping> mappings, String protein) {
    for (IdMapping mapping : mappings) {
      if (mapping.getProtein().equals(protein)) {
        return mapping;
      }
    }
    return null;
  }

  @Test
  public void parse() throws Throwable {
    Path file =
        Paths.get(getClass().getResource("/annotation/UP000005640_9606_small.idmapping").toURI());

    try (BufferedReader reader = Files.newBufferedReader(file, Charset.forName("UTF-8"))) {
      List<IdMapping> mappings = idMappingParser.parse(reader);

      assertEquals(3, mappings.size());
      IdMapping mapping = find(mappings, "F5GX34");
      assertNotNull(mapping);
      assertEquals("F5GX34", mapping.getProtein());
      assertTrue(mapping.getMappings().containsKey("UniProtKB-ID"));
      List<String> values = mapping.getMappings().get("UniProtKB-ID");
      assertEquals(1, values.size());
      assertEquals("F5GX34_HUMAN", values.get(0));
      assertTrue(mapping.getMappings().containsKey("EMBL"));
      values = mapping.getMappings().get("EMBL");
      assertEquals(2, values.size());
      assertEquals("AC007215", values.get(0));
      assertEquals("AC008115", values.get(1));
      assertTrue(mapping.getMappings().containsKey("Ensembl_TRS"));
      values = mapping.getMappings().get("Ensembl_TRS");
      assertEquals(2, values.size());
      assertEquals("ENST00000534843", values.get(0));
      assertEquals("ENST00000540583", values.get(1));
      assertFalse(mapping.getMappings().containsKey("GI"));

      mapping = find(mappings, "A0A0G2JM44");
      assertNotNull(mapping);
      assertEquals("A0A0G2JM44", mapping.getProtein());
      assertTrue(mapping.getMappings().containsKey("UniProtKB-ID"));
      values = mapping.getMappings().get("UniProtKB-ID");
      assertEquals(1, values.size());
      assertEquals("A0A0G2JM44_HUMAN", values.get(0));
      assertTrue(mapping.getMappings().containsKey("EMBL"));
      values = mapping.getMappings().get("EMBL");
      assertEquals(1, values.size());
      assertEquals("AC245128", values.get(0));
      assertFalse(mapping.getMappings().containsKey("GI"));

      mapping = find(mappings, "Q8NCP5");
      assertNotNull(mapping);
      assertEquals("Q8NCP5", mapping.getProtein());
      assertTrue(mapping.getMappings().containsKey("UniProtKB-ID"));
      values = mapping.getMappings().get("UniProtKB-ID");
      assertEquals(1, values.size());
      assertEquals("ZBT44_HUMAN", values.get(0));
      assertTrue(mapping.getMappings().containsKey("EMBL"));
      values = mapping.getMappings().get("EMBL");
      assertEquals(4, values.size());
      assertEquals("BC030580", values.get(0));
      assertEquals("BC049375", values.get(1));
      assertEquals("BC050723", values.get(2));
      assertEquals("BC071729", values.get(3));
      assertTrue(mapping.getMappings().containsKey("GI"));
      values = mapping.getMappings().get("GI");
      assertEquals(8, values.size());
      assertEquals("29387146", values.get(0));
      assertEquals("21040475", values.get(1));
      assertEquals("109134351", values.get(2));
      assertEquals("666875845", values.get(3));
      assertEquals("666875861", values.get(4));
      assertEquals("74760158", values.get(5));
      assertEquals("30047807", values.get(6));
      assertEquals("47940103", values.get(7));
    }
  }

  @Test
  public void parse_Empty() throws Throwable {
    try (BufferedReader reader = new BufferedReader(new StringReader(""))) {
      List<IdMapping> mappings = idMappingParser.parse(reader);

      assertTrue(mappings.isEmpty());
    }
  }

  @Test
  public void parse_Consumer() throws Throwable {
    Path file =
        Paths.get(getClass().getResource("/annotation/UP000005640_9606_small.idmapping").toURI());

    try (BufferedReader reader = Files.newBufferedReader(file, Charset.forName("UTF-8"))) {
      idMappingParser.parse(reader, handler);

      verify(handler, times(3)).accept(idMappingCaptor.capture());
      List<IdMapping> mappings = idMappingCaptor.getAllValues();
      IdMapping mapping = find(mappings, "F5GX34");
      assertNotNull(mapping);
      assertEquals("F5GX34", mapping.getProtein());
      assertTrue(mapping.getMappings().containsKey("UniProtKB-ID"));
      List<String> values = mapping.getMappings().get("UniProtKB-ID");
      assertEquals(1, values.size());
      assertEquals("F5GX34_HUMAN", values.get(0));
      assertTrue(mapping.getMappings().containsKey("EMBL"));
      values = mapping.getMappings().get("EMBL");
      assertEquals(2, values.size());
      assertEquals("AC007215", values.get(0));
      assertEquals("AC008115", values.get(1));
      assertTrue(mapping.getMappings().containsKey("Ensembl_TRS"));
      values = mapping.getMappings().get("Ensembl_TRS");
      assertEquals(2, values.size());
      assertEquals("ENST00000534843", values.get(0));
      assertEquals("ENST00000540583", values.get(1));
      assertFalse(mapping.getMappings().containsKey("GI"));

      mapping = find(mappings, "A0A0G2JM44");
      assertNotNull(mapping);
      assertEquals("A0A0G2JM44", mapping.getProtein());
      assertTrue(mapping.getMappings().containsKey("UniProtKB-ID"));
      values = mapping.getMappings().get("UniProtKB-ID");
      assertEquals(1, values.size());
      assertEquals("A0A0G2JM44_HUMAN", values.get(0));
      assertTrue(mapping.getMappings().containsKey("EMBL"));
      values = mapping.getMappings().get("EMBL");
      assertEquals(1, values.size());
      assertEquals("AC245128", values.get(0));
      assertFalse(mapping.getMappings().containsKey("GI"));

      mapping = find(mappings, "Q8NCP5");
      assertNotNull(mapping);
      assertEquals("Q8NCP5", mapping.getProtein());
      assertTrue(mapping.getMappings().containsKey("UniProtKB-ID"));
      values = mapping.getMappings().get("UniProtKB-ID");
      assertEquals(1, values.size());
      assertEquals("ZBT44_HUMAN", values.get(0));
      assertTrue(mapping.getMappings().containsKey("EMBL"));
      values = mapping.getMappings().get("EMBL");
      assertEquals(4, values.size());
      assertEquals("BC030580", values.get(0));
      assertEquals("BC049375", values.get(1));
      assertEquals("BC050723", values.get(2));
      assertEquals("BC071729", values.get(3));
      assertTrue(mapping.getMappings().containsKey("GI"));
      values = mapping.getMappings().get("GI");
      assertEquals(8, values.size());
      assertEquals("29387146", values.get(0));
      assertEquals("21040475", values.get(1));
      assertEquals("109134351", values.get(2));
      assertEquals("666875845", values.get(3));
      assertEquals("666875861", values.get(4));
      assertEquals("74760158", values.get(5));
      assertEquals("30047807", values.get(6));
      assertEquals("47940103", values.get(7));
    }
  }

  @Test
  public void parse_Consumer_Empty() throws Throwable {
    try (BufferedReader reader = new BufferedReader(new StringReader(""))) {
      idMappingParser.parse(reader, handler);

      verify(handler, never()).accept(any(IdMapping.class));
    }
  }
}
