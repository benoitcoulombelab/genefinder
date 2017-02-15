package ca.qc.ircm.genefinder.gui;

import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Splash screen.
 */
public class SplashScreen {
  private Stage stage;

  /**
   * Creates splash screen window.
   *
   * @param stage
   *          stage
   */
  public SplashScreen(Stage stage) {
    this.stage = stage;
    stage.initStyle(StageStyle.UNDECORATED);
    SplashScreenView view = new SplashScreenView();
    Parent viewNode = view.getView();
    viewNode.setCursor(Cursor.WAIT);
    Scene scene = new Scene(viewNode);
    stage.setScene(scene);
  }

  public void show() {
    stage.show();
  }

  public void hide() {
    stage.hide();
  }
}
