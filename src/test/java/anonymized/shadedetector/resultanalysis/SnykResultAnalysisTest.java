package anonymized.shadedetector.resultanalysis;

import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class SnykResultAnalysisTest {

    @Test
    public void testEmptyReport() {
        Path report = Path.of(SnykResultAnalysisTest.class.getResource("/sca/snyk/snyk-empty.json").getPath());
        Set<String> cves = new SnykResultAnalysis().getDetectedCVEs(report);
        assertTrue(cves.isEmpty());
    }

    @Test
    public void testNonEmptyReport() {
        Path report = Path.of(SnykResultAnalysisTest.class.getResource("/sca/snyk/snyk-nonempty.json").getPath());
        Set<String> cves = new SnykResultAnalysis().getDetectedCVEs(report);
        Set<String> expected = Set.of("CVE-2022-25857","CVE-2022-38749","CVE-2022-38752","CVE-2022-38750","CVE-2022-38751","CVE-2022-41854","CVE-2022-1471","CVE-2017-18640");
        assertEquals(expected.size(),cves.size());
        assertEquals(expected,cves);
    }

    @Test
    public void testNotExisting() {
        assertThrows(IllegalArgumentException.class,() -> new SnykResultAnalysis().getDetectedCVEs(Path.of("____foo")));
    }
}
