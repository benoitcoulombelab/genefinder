package ca.qc.ircm.genefinder.util;

import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stackoverflowusers.file.WindowsShortcut;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

/**
 * Utilities for files.
 */
public class FileUtils {
  private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

  /**
   * Resolves Windows shortcut.
   *
   * @param file
   *          file that can be a Windows shortcut
   * @return resolved Windows shortcut or file parameter if not a Windows shortcut
   */
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
