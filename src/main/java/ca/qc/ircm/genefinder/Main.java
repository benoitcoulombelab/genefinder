package ca.qc.ircm.genefinder;

import ca.qc.ircm.genefinder.gui.ApplicationGui;
import ca.qc.ircm.util.javafx.SpringAfterburnerInstanceSupplier;
import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * JavaFX application.
 */
@SpringBootApplication
public class Main extends AbstractSpringBootJavafxApplication {
  @Override
  public void init() throws Exception {
    super.init();
    com.airhacks.afterburner.injection.Injector
        .setInstanceSupplier(new SpringAfterburnerInstanceSupplier(applicationContext));
  }

  @Override
  public void start(Stage stage) throws Exception {
    ApplicationGui app = new ApplicationGui();
    notifyPreloader(new ApplicationStarted());
    app.show();
  }

  public static void main(String[] args) {
    System.setProperty("javafx.preloader", MainPreloader.class.getName());
    Application.launch(args);
  }
}
