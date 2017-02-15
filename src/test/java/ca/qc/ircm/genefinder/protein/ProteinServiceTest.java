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
