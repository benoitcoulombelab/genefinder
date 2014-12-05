package ca.qc.ircm.genefinder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibraryLoaderBean implements LibraryLoader {
    private final Logger logger = LoggerFactory.getLogger(LibraryLoaderBean.class);
    @Inject
    private ApplicationProperties applicationProperties;

    protected LibraryLoaderBean() {
    }

    public LibraryLoaderBean(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void loadJShortcutLibrary() throws IOException {
        File dllDirectory = applicationProperties.getHome();
        extractDll(dllDirectory);
        try {
            logger.info("Loading 32 bits library.");
            File library = dll32(dllDirectory);
            System.load(library.getAbsolutePath());
            prepareForJShortcut(library);
        } catch (Throwable e) {
            try {
                logger.info("Loading 64 bits library.");
                File library = dll64(dllDirectory);
                System.load(library.getAbsolutePath());
                prepareForJShortcut(library);
            } catch (Throwable e2) {
                logger.error("Could not load jshortcut dll", e);
                logger.error("Could not load jshortcut dll", e2);
                throw new IOException("Could not load jshortcut dll", e);
            }
        }
    }

    public boolean is64bitWindows() {
        boolean is64bit = System.getProperty("os.arch").indexOf("64") != -1;
        return System.getenv("ProgramFiles(x86)") != null || is64bit;
    }

    private void extractDll(File dllDirectory) throws IOException {
        if ((!dllDirectory.exists() && !dllDirectory.mkdirs()) || !dllDirectory.isDirectory()) {
            throw new IOException("Could not create directory " + dllDirectory + " where required DLL are stored");
        }
        File dll32 = dll32(dllDirectory);
        if (!dll32.exists()) {
            try (InputStream input = getClass().getResourceAsStream("/jshortcut_x86-0.4.dll");
                    OutputStream output = new FileOutputStream(dll32)) {
                IOUtils.copyLarge(input, output);
            }
        }
        File dll64 = dll64(dllDirectory);
        if (!dll64.exists()) {
            try (InputStream input = getClass().getResourceAsStream("/jshortcut_amd64-0.4.dll");
                    OutputStream output = new FileOutputStream(dll64)) {
                IOUtils.copyLarge(input, output);
            }
        }
    }

    private void prepareForJShortcut(File library) throws IOException {
        FileUtils.copyFile(library, new File(library.getParentFile(), "jshortcut.dll"));
        System.setProperty("JSHORTCUT_HOME", library.getParentFile().getPath());
    }

    private File dll32(File dllDirectory) {
        return new File(dllDirectory, "jshortcut_x86-0.4.dll");
    }

    private File dll64(File dllDirectory) {
        return new File(dllDirectory, "jshortcut_amd64-0.4.dll");
    }
}
