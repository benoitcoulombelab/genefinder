package ca.qc.ircm.genefinder.test.config;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allow test to be tried again on fail.
 */
public class LogMethodNameRule implements TestRule {
    private static final Logger logger = LoggerFactory.getLogger(LogMethodNameRule.class);

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                logger.debug("Running test {}", description.getMethodName());
                base.evaluate();
            }
        };
    }
}