package ca.qc.ircm.genefinder;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.nio.file.Paths;

public class StringToPathConverterTest {
  private StringToPathConverter pathConverter = new StringToPathConverter();

  @Test
  public void convert() {
    assertEquals(Paths.get("test.txt"), pathConverter.convert("test.txt"));
    assertEquals(Paths.get("/usr/local/test.txt"), pathConverter.convert("/usr/local/test.txt"));
    assertEquals(Paths.get("c:\\test.txt"), pathConverter.convert("c:\\test.txt"));
    assertEquals(Paths.get("${user.home}/test.txt"),
        pathConverter.convert("${user.home}/test.txt"));
  }
}
