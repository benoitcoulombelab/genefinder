package ca.qc.ircm.genefinder.test.config;

import java.util.List;

import org.junit.rules.TestRule;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
 * Runner that configures logging.
 */
public class TestLoggingRunner extends BlockJUnit4ClassRunner {

    public TestLoggingRunner(final Class<?> clazz) throws InitializationError {
        super(clazz);
    }

    @Override
    protected List<TestRule> getTestRules(final Object target) {
        List<TestRule> rules = super.getTestRules(target);
        rules.add(0, new LogMethodNameRule());
        rules.add(0, new RetryOnFailRule());
        rules.add(0, new MockitoRule(target));
        return rules;
    }
}
