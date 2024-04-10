package anonymized.shadedetector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * This is necessary because it turns out that File::deleteOnExit() will only delete a directory if it is empty.
 * Obviously, most temporary directories someone might want to create will not wind up empty.
 * This class also gives more control over when the directories are deleted.
 */
public class TempDirectory {
    private static final Logger LOGGER = LoggerFactory.getLogger(Cache.class);
    private static Path TEMP_ROOT = null;   // Set lazily in getRoot() to pick up -cache from command line

    private static HashMap<String, ArrayList<Path>> dirsByPrefix = new LinkedHashMap<>();

    public static synchronized Path getRoot() {
        if (TEMP_ROOT == null) {
            setRoot(Cache.getRoot().toPath());
        }

        return TEMP_ROOT;
    }

    public static synchronized void setRoot(Path newRoot) throws IllegalStateException {
        if (TEMP_ROOT != null) {
            throw new IllegalStateException("Attempt to set TempDirectory root more than once");
        }

        TEMP_ROOT = newRoot;
    }

    public static synchronized Path create(String prefix) throws IOException {
        Path tempDir = Files.createTempDirectory(getRoot(), prefix);
        if (!dirsByPrefix.containsKey(prefix)) {
            dirsByPrefix.put(prefix, new ArrayList<>());
        }
        dirsByPrefix.get(prefix).add(tempDir);
        return tempDir;
    }

    /**
     * Recursively delete all files and directories in the given directory.
     * This method may be slow, since it iterates the complete list of all paths with the given prefix.
     * @return true if the dir was successfully deleted, false otherwise
     */
    public static synchronized boolean delete(String prefix, Path dir) {
        if (!dirsByPrefix.containsKey(prefix) || !dirsByPrefix.get(prefix).remove(dir)) {
            return false;
        }

        return recursivelyDeleteQuietly(dir.toFile());
    }

    /**
     * Recursively delete all temp directories created with the given prefix.
     * Prefer calling this once to calling delete() on each directory.
     * @return true if all dirs were successfully deleted, false otherwise
     */
    public static synchronized boolean deleteAllForPrefix(String prefix) {
        if (!dirsByPrefix.containsKey(prefix)) {
            return true;
        }

        boolean success = true;
        for (Path p : dirsByPrefix.get(prefix)) {
            success = recursivelyDeleteQuietly(p.toFile()) && success;
        }

        dirsByPrefix.remove(prefix);
        return success;
    }

    /**
     * Recursively delete all temp directories created with any prefix.
     * @return true if all dirs were successfully deleted, false otherwise
     */
    public static synchronized boolean deleteAll() {
        boolean success = true;
        for (String prefix : dirsByPrefix.keySet()) {
            success = deleteAllForPrefix(prefix) && success;
        }

        dirsByPrefix.clear();
        return success;
    }

    private static boolean recursivelyDeleteQuietly(File dirOrFile) {
        try {
            recursivelyDelete(dirOrFile);
        } catch (IOException e) {
            LOGGER.error("Error recursively deleting {}: ", dirOrFile, e);
            return false;
        }

        return true;
    }

    // From https://stackoverflow.com/a/779529/47984
    private static void recursivelyDelete(File dirOrFile) throws FileNotFoundException {
        if (dirOrFile.isDirectory()) {
            File[] files = dirOrFile.listFiles();
            // It couldn't possibly be null, could it? Yes it could: https://github.com/jensdietrich/shadedetector/issues/24
            if (files != null) {
                for (File c : files) {
                    recursivelyDelete(c);
                }
            }
        }

        if (!dirOrFile.delete()) {
            throw new FileNotFoundException("Failed to delete file: " + dirOrFile);
        }
    }

    // Ensure that all temp dirs are deleted before the program exits. Based on java.io.DeleteOnExitHook.
    static {
//        Runtime.getRuntime().addShutdownHook(new Thread(TempDirectory::deleteAll));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Can't use LOGGER -- it may have been cleaned up by now
            System.err.println("TempDirectory: About to delete temp dirs for " + dirsByPrefix.size() + " prefixes");
            deleteAll();
            System.err.println("TempDirectory: Finished deleting temp dirs");
        }));
    }
}
