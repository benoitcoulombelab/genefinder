package ca.qc.ircm.genefinder;

import ca.qc.ircm.genefinder.data.DataService;
import ca.qc.ircm.genefinder.data.DataServiceBean;
import ca.qc.ircm.genefinder.data.DataWriter;
import ca.qc.ircm.genefinder.data.FindGenesInDataTask;
import ca.qc.ircm.genefinder.data.FindGenesInDataTaskFactory;
import ca.qc.ircm.genefinder.data.GuessDataWriter;
import ca.qc.ircm.genefinder.maxquant.FindGenesInMaxQuantTask;
import ca.qc.ircm.genefinder.maxquant.FindGenesInMaxQuantTaskFactory;
import ca.qc.ircm.genefinder.maxquant.MaxQuantService;
import ca.qc.ircm.genefinder.maxquant.MaxQuantServiceBean;
import ca.qc.ircm.genefinder.ncbi.NcbiService;
import ca.qc.ircm.genefinder.ncbi.NcbiServiceBean;
import ca.qc.ircm.genefinder.organism.OrganismService;
import ca.qc.ircm.genefinder.organism.OrganismServiceBean;
import ca.qc.ircm.protein.ProteinService;
import ca.qc.ircm.protein.ProteinServiceDefault;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * Guice module that binds classes for application.
 */
public class ApplicationModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ApplicationProperties.class).toProvider(ApplicationPropertiesBeanProvider.class);
        bind(LibraryLoader.class).to(LibraryLoaderBean.class);
        bind(DataService.class).to(DataServiceBean.class);
        bind(DataWriter.class).to(GuessDataWriter.class);
        install(new FactoryModuleBuilder().implement(FindGenesInDataTask.class, FindGenesInDataTask.class).build(
                FindGenesInDataTaskFactory.class));
        install(new FactoryModuleBuilder().implement(FindGenesInMaxQuantTask.class, FindGenesInMaxQuantTask.class)
                .build(FindGenesInMaxQuantTaskFactory.class));
        bind(MaxQuantService.class).to(MaxQuantServiceBean.class);
        bind(NcbiService.class).to(NcbiServiceBean.class);
        bind(OrganismService.class).to(OrganismServiceBean.class);
        bind(ProteinService.class).to(ProteinServiceDefault.class);
    }
}