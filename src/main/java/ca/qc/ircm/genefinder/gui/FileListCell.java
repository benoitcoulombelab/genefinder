package ca.qc.ircm.genefinder.gui;

import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.util.StringConverter;

import java.io.File;

/**
 * {@link ListCell} that shows files.
 */
public class FileListCell extends TextFieldListCell<File> {
  private static class FileConverter extends StringConverter<File> {
    @Override
    public File fromString(String input) {
      return new File(input);
    }

    @Override
    public String toString(File file) {
      return file != null ? file.getPath() : "";
    }
  }

  public FileListCell() {
    super(new FileConverter());
  }

  @Override
  public void updateItem(File file, boolean empty) {
    super.updateItem(file, empty);
    if (file != null) {
      setText(file.getName());
      setTooltip(new Tooltip(file.getPath()));
    } else {
      setText(null);
      setTooltip(null);
    }
  }

  @Override
  public void cancelEdit() {
    super.cancelEdit();
    File file = getItem();
    if (file != null) {
      setText(file.getName());
      setTooltip(new Tooltip(file.getPath()));
    } else {
      setText(null);
      setTooltip(null);
    }
  }
}
