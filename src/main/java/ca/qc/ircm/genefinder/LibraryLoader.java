package ca.qc.ircm.genefinder;

import java.io.IOException;

/**
 * Loads DLLs.
 */
public interface LibraryLoader {
    /**
     * Loads JShortcut's DLL.
     *
     * @throws IOException
     *             could not extract DLL from jar
     */
    public void loadJShortcutLibrary() throws IOException;
}
