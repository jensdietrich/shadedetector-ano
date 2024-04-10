package anonymized.shadedetector.resultanalysis;

import java.nio.file.Path;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GrypeResultAnalysisTest {

    @Test
    public void testEmptyReport() {
        Path report = Path.of(GrypeResultAnalysisTest.class.getResource("/sca/grype/grype-empty.json").getPath());
        Set<String> cves = new GrypeResultAnalysis().getDetectedCVEs(report);
        assertTrue(cves.isEmpty());
    }

    @Test
    public void testNonEmptyReport() {
        Path report = Path.of(GrypeResultAnalysisTest.class.getResource("/sca/grype/grype-nonempty.json").getPath());
        Set<String> cves = new GrypeResultAnalysis().getDetectedCVEs(report);
        Set<String> expected = Set.of("CVE-2022-25857","CVE-2022-38749","CVE-2022-38752","CVE-2022-38750","CVE-2022-38751","CVE-2022-41854","CVE-2022-1471","CVE-2017-18640");
        assertEquals(expected.size(),cves.size());
        assertEquals(expected,cves);
    }

    @Test
    public void testNotExisting() {
        assertThrows(IllegalArgumentException.class,() -> new GrypeResultAnalysis().getDetectedCVEs(Path.of("____foo")));
    }
}
