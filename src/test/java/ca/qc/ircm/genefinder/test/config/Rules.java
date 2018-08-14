/*
 * Copyright (c) 2014 Institut de recherches cliniques de Montreal (IRCM)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ca.qc.ircm.genefinder.test.config;

import java.util.ArrayList;
import java.util.List;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

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
