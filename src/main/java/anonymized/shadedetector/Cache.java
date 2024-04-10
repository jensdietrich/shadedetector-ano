package anonymized.shadedetector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;

/**
 * Defines the cache root. Default can be changed, this is useful for testing.
 * The root must be set before cache folders are used , when the application is started.
 * @author jens dietrich
 */
public class Cache {
    private static Logger LOGGER = LoggerFactory.getLogger(Cache.class);
    private static File ROOT = new File(".cache");

    public static File getRoot() {
        return ROOT;
    }

    public static void setRoot(File dir) {
        Cache.ROOT = ensureDirExists(dir);
    }

    public static File getCache(String name) {
        return ensureDirExists(new File(ROOT,name));
    }

    public static boolean clearCache(String name) {
        File dir = new File(ROOT,name);
        if (dir.exists()) {
            LOGGER.info("Clearing cache folder {}",dir.getAbsolutePath());
            return dir.delete();
        }
        return true;
    }

    private static File ensureDirExists(File dir) {
        if (!dir.exists()) {
            LOGGER.info("Creating cache folder {}",dir.getAbsolutePath());
            dir.mkdirs();
        }

        return dir; // For convenience
    }
}
