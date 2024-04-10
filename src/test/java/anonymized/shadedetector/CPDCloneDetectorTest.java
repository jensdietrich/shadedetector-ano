package anonymized.shadedetector;

import anonymized.shadedetector.clonedetection.pmd.CPDBasedCloneDetector;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public class CPDCloneDetectorTest {

    @Test
    public void testCPDCloneDetection() throws Exception {
        CPDBasedCloneDetector detector = new CPDBasedCloneDetector();
        URL resourceUrl = getClass().getResource("/f.jar");
        Path fJarPath = Paths.get(resourceUrl.toURI());
        resourceUrl = getClass().getResource("/b.jar");
        Path bJarPath = Paths.get(resourceUrl.toURI());
        Set<CloneDetector.CloneRecord> detect = detector.detect(fJarPath, bJarPath);
    }
}
