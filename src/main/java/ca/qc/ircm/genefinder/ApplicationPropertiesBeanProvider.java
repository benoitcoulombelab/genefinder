package ca.qc.ircm.genefinder;

import javax.inject.Provider;

/**
 * Creates an instance of {@link ApplicationProperties}.
 */
public class ApplicationPropertiesBeanProvider implements Provider<ApplicationProperties> {
    @Override
    public ApplicationProperties get() {
        return new ApplicationPropertiesBean() {
            {
                init();
            }
        };
    }
}
