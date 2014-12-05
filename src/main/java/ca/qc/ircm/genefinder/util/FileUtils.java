package ca.qc.ircm.genefinder.util;

import java.io.File;

import net.jimmc.jshortcut.JShellLink;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for files.
 */
public class FileUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    public static File resolveWindowsShorcut(File file) {
        if (SystemUtils.IS_OS_WINDOWS && file.getName().endsWith(".lnk")) {
            try {
                JShellLink link = new JShellLink(file.getParent(), file.getName());
                link.load();
                return new File(link.getPath());
            } catch (Exception e) {
                logger.debug("Could not resolve link {}", file, e);
                return file;
            }
        } else {
            return file;
        }
    }
}
