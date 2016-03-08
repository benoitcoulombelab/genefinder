package ca.qc.ircm.genefinder;

import com.google.inject.Guice;
import com.google.inject.Injector;

import ca.qc.ircm.genefinder.gui.ApplicationGui;
import ca.qc.ircm.genefinder.gui.SplashScreen;
import ca.qc.ircm.util.javafx.AfterburnerGuiceInstanceSupplier;
import ca.qc.ircm.util.javafx.message.MessageDialog;
import ca.qc.ircm.util.javafx.message.MessageDialog.MessageDialogType;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * JavaFX application.
 */
public class Main extends Application {
  private static final Logger logger = LoggerFactory.getLogger(Main.class);
  private ResourceBundle bundle;

  @Override
  public void init() throws Exception {
    bundle = ResourceBundle.getBundle(getClass().getName(), Locale.getDefault());
  }

  @Override
  public void start(Stage stage) throws Exception {
    SplashScreen splash = new SplashScreen();
    splash.show();

    // Initialise application in background.
    class InitialiseTask extends Task<Void> {
      @Override
      public Void call() throws Exception {
        Injector injector = Guice.createInjector(new ApplicationModule());
        com.airhacks.afterburner.injection.Injector
            .setInstanceSupplier(new AfterburnerGuiceInstanceSupplier(injector));

        Platform.runLater(() -> {
          startApp();
        });
        return null;
      }
    }

    final InitialiseTask initialiseTask = new InitialiseTask();
    initialiseTask.setOnSucceeded(event -> splash.hide());
    initialiseTask.setOnFailed(event -> {
      splash.hide();
      logger.error("Could not start application", initialiseTask.getException());
      new MessageDialog(stage, MessageDialogType.ERROR, bundle.getString("error.title"),
          initialiseTask.getException().getMessage());
    });
    Thread thread = new Thread(initialiseTask);
    thread.start();
  }

  private void startApp() {
    ApplicationGui app = new ApplicationGui();
    app.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
