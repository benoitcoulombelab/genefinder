package ca.qc.ircm.genefinder.gui.drag.list;

import java.io.File;
import java.util.List;

import javafx.event.EventHandler;
import javafx.scene.control.ListView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

/**
 * Handles drag detected for TextField containing file path.
 */
public class DragFileOnListDetectedHandler implements EventHandler<MouseEvent> {
    protected final ListView<File> list;
    protected final TransferMode[] transferModes;

    public DragFileOnListDetectedHandler(ListView<File> list) {
        this(list, TransferMode.ANY);
    }

    public DragFileOnListDetectedHandler(ListView<File> list, TransferMode... transferModes) {
        this.list = list;
        this.transferModes = transferModes;
    }

    @Override
    public void handle(MouseEvent event) {
        Dragboard db = list.startDragAndDrop(transferModes);
        ClipboardContent content = new ClipboardContent();
        List<File> files = list.getSelectionModel().getSelectedItems();
        content.putFiles(files);
        StringBuilder builder = new StringBuilder();
        files.forEach(file -> {
            builder.append("\n");
            builder.append(file.getAbsolutePath());
        });
        if (builder.length() > 0)
            builder.deleteCharAt(0);
        content.putString(builder.toString());
        db.setContent(content);
        event.consume();
    }
}
