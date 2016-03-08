package ca.qc.ircm.genefinder.test.config;

import ca.qc.ircm.genefinder.ApplicationPropertiesBean;

/**
 * Provider for application properties.
 */
public class ApplicationPropertiesBeanProvider {
  public ApplicationPropertiesBeanProvider() {
  }

  /**
   * Returns an ApplicationPropertiesBean.
   * 
   * @return an ApplicationPropertiesBean
   */
  public ApplicationPropertiesBean get() {
    ApplicationPropertiesBean applicationPropertiesBean = new ApplicationPropertiesBean() {
      {
        init();
      }
    };
    return applicationPropertiesBean;
  }
}
