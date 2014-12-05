package ca.qc.ircm.protein;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import ca.qc.ircm.genefinder.test.config.TestLoggingRunner;

@RunWith(TestLoggingRunner.class)
public class ProteinServiceDefaultTest {
    private ProteinServiceDefault proteinServiceDefault;

    @Before
    public void beforeTest() throws Throwable {
        proteinServiceDefault = new ProteinServiceDefault();
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