package anonymized.shadedetector;

import anonymized.shadedetector.clonedetection.ast.ASTBasedCloneDetector;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CloneDetectorFactoryTest {

    @Test
    public void testSelectAll() {
        CloneDetector cloneDetector = new CloneDetectorFactory().create("ast");
        assertNotNull(cloneDetector);
        assertEquals("ast",cloneDetector.name());
        assertTrue(cloneDetector instanceof ASTBasedCloneDetector);
    }

    @Test
    public void testNonExisting() {
        assertThrows(IllegalArgumentException.class, () -> new CloneDetectorFactory().create("foo"));
    }
}
