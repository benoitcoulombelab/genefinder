package ca.qc.ircm.genefinder.gui.drag.textfield;

import java.io.File;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import ca.qc.ircm.genefinder.gui.drag.DragOverHandler;
import ca.qc.ircm.genefinder.util.FileUtils;

/**
 * Handles drag over for TextField containing directory path.
 */
public class DragDirectoryOverHandler extends DragOverHandler implements EventHandler<DragEvent> {
    private final Node dragNode;

    public DragDirectoryOverHandler(Node dropNode, Node dragNode) {
        super(dropNode);
        this.dragNode = dragNode;
    }

    @Override
    protected boolean accept(DragEvent event) {
        boolean accept = false;
        if (event.getGestureSource() != dragNode) {
            // Accept directory.
            accept |= event.getDragboard().hasFiles() && event.getDragboard().getFiles().size() == 1
                    && validDirectory(event.getDragboard().getFiles().get(0));
            // Accept string with a single line.
            accept |= event.getDragboard().hasString() && !event.getDragboard().getString().contains("\n");
        }
        return accept;
    }

    private boolean validDirectory(File file) {
        file = FileUtils.resolveWindowsShorcut(file);
        return file.isDirectory();
    }

    @Override
    protected void setAcceptTransferModes(DragEvent event) {
        event.acceptTransferModes(TransferMode.COPY);
    }
}
