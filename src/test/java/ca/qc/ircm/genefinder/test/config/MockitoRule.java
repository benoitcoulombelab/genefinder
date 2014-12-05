package ca.qc.ircm.genefinder.test.config;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Inject mocks/captors before tests and validate Mockito usage after tests.
 */
public class MockitoRule implements TestRule {
    private final Object target;

    public MockitoRule(Object target) {
        this.target = target;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                MockitoAnnotations.initMocks(target);
                base.evaluate();
                Mockito.validateMockitoUsage();
            }
        };
    }
}