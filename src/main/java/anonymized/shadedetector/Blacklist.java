package anonymized.shadedetector;

/**
 * List of artifacts (GAVSs) for which the analysis is problematic.
 * TODO: move into file.
 * @author jens dietrich
 */
public class Blacklist {

    static boolean contains (Artifact artifact) {
        // outofmemory when extracting from jar -- check for zip bomb or similar
        // might be related to https://bugs.openjdk.org/browse/JDK-7143743
        // TODO investigate whether this only happends for certain JDK versions (11, 17 seems to be fine)
        // OutOfMemoryError no longer occurs, and a ZipException that does occur is caught and logged, so
        // there is no need to keep this in the blacklist.
//        return artifact.getGroupId().equals("dev.dejvokep") ;
        return false;
    }
}
