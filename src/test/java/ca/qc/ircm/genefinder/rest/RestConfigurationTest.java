package ca.qc.ircm.genefinder.rest;

import static org.junit.Assert.assertEquals;

import ca.qc.ircm.genefinder.test.config.ServiceTestAnnotations;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class RestConfigurationTest {
  @Inject
  private RestConfiguration restConfiguration;

  @Test
  public void defaultProperties() throws Throwable {
    assertEquals(120000, restConfiguration.timeout());
  }
}
