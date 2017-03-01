/*
 * Copyright (c) 2014 Institut de recherches cliniques de Montreal (IRCM)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

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
