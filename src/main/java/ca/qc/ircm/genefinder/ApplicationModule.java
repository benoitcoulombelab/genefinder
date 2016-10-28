package ca.qc.ircm.genefinder;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import ca.qc.ircm.genefinder.annotation.DownloadProteinMappingServiceBean;
import ca.qc.ircm.genefinder.annotation.ProteinMappingService;
import ca.qc.ircm.genefinder.data.DataService;
import ca.qc.ircm.genefinder.data.DataServiceBean;
import ca.qc.ircm.genefinder.data.DataWriter;
import ca.qc.ircm.genefinder.data.FindGenesInDataTask;
import ca.qc.ircm.genefinder.data.FindGenesInDataTaskFactory;
import ca.qc.ircm.genefinder.data.GuessDataWriter;
import ca.qc.ircm.genefinder.net.FtpClientFactory;
import ca.qc.ircm.genefinder.net.FtpClientFactoryBean;
import ca.qc.ircm.genefinder.organism.OrganismService;
import ca.qc.ircm.genefinder.organism.OrganismServiceBean;
import ca.qc.ircm.protein.ProteinService;
import ca.qc.ircm.protein.ProteinServiceDefault;

/**
 * Guice module that binds classes for application.
 */
public class ApplicationModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(ApplicationProperties.class).toProvider(ApplicationPropertiesBeanProvider.class);
    bind(ProteinMappingService.class).to(DownloadProteinMappingServiceBean.class);
    bind(DataService.class).to(DataServiceBean.class);
    bind(DataWriter.class).to(GuessDataWriter.class);
    install(
        new FactoryModuleBuilder().implement(FindGenesInDataTask.class, FindGenesInDataTask.class)
            .build(FindGenesInDataTaskFactory.class));
    bind(FtpClientFactory.class).to(FtpClientFactoryBean.class);
    bind(OrganismService.class).to(OrganismServiceBean.class);
    bind(ProteinService.class).to(ProteinServiceDefault.class);
  }
}
