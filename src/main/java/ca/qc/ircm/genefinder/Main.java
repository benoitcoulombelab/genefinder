package ca.qc.ircm.genefinder;

import ca.qc.ircm.genefinder.gui.ApplicationGui;
import ca.qc.ircm.genefinder.organism.Organism;
import ca.qc.ircm.genefinder.organism.OrganismService;
import ca.qc.ircm.util.javafx.SpringAfterburnerInstanceSupplier;
import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.inject.Inject;

/**
 * JavaFX application.
 */
@SpringBootApplication
public class Main extends AbstractSpringBootJavafxApplication {
  @Inject
  private OrganismService organismService;

  @Override
  public void init() throws Exception {
    super.init();
    com.airhacks.afterburner.injection.Injector
        .setInstanceSupplier(new SpringAfterburnerInstanceSupplier(applicationContext));
  }

  private void insertOrganismsIfMissing() {
    if (!organismService.containsAny()) {
      organismService.insert(new Organism(9606, "Homo sapiens"));
    }
  }

  @Override
  public void start(Stage stage) throws Exception {
    insertOrganismsIfMissing();
    ApplicationGui app = new ApplicationGui();
    notifyPreloader(new ApplicationStarted());
    app.show();
  }

  public static void main(String[] args) {
    System.setProperty("javafx.preloader", MainPreloader.class.getName());
    Application.launch(args);
  }
}
