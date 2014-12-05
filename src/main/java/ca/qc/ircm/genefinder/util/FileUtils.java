package ca.qc.ircm.genefinder.util;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stackoverflowusers.file.WindowsShortcut;

/**
 * Utilities for files.
 */
public class FileUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    public static File resolveWindowsShorcut(File file) {
        if (SystemUtils.IS_OS_WINDOWS && file.getName().endsWith(".lnk")) {
            try {
                FileSystemManager fileSystemManager = VFS.getManager();
                FileObject fileObject = fileSystemManager.resolveFile(file.getPath());
                if (WindowsShortcut.isPotentialValidLink(fileObject)) {
                    WindowsShortcut shortcut = new WindowsShortcut(file);
                    return new File(shortcut.getRealFilename());
                } else {
                    return file;
                }
            } catch (IOException | ParseException e) {
                logger.debug("Could not resolve link {}", file, e);
                return file;
            }
        } else {
            return file;
        }
    }
}
