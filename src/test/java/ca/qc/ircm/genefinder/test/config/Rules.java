package ca.qc.ircm.genefinder.test.config;

import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Default test rules.
 */
public class Rules {
  /**
   * Returns rules to use for all tests.
   *
   * @param target
   *          test class instance
   * @return rules to use for all tests
   */
  public static RuleChain defaultRules(Object target) {
    RuleChain ruleChain = RuleChain.emptyRuleChain();
    for (TestRule rule : getRules(target)) {
      ruleChain = ruleChain.around(rule);
    }
    return ruleChain;
  }

  private static List<TestRule> getRules(Object target) {
    List<TestRule> rules = new ArrayList<>();
    rules.add(new LogMethodNameRule());
    rules.add(new RetryOnFailRule());
    rules.add(new MockitoRule(target));
    return rules;
  }
}
