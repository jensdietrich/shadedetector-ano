package anonymized.shadedetector.resultanalysis;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class SteadyResultAnalysisTest {

    @Test
    public void testEmptyReport() {
        Path report = Path.of(SteadyResultAnalysisTest.class.getResource("/sca/steady/steady-empty.json").getPath());
        Set<String> cves = new SteadyResultAnalysis().getDetectedCVEs(report);
        assertTrue(cves.isEmpty());
    }

    @Test
    public void testNonEmptyReport() {
        Path report = Path.of(SteadyResultAnalysisTest.class.getResource("/sca/steady/steady-nonempty.json").getPath());
        Set<String> cves = new SteadyResultAnalysis().getDetectedCVEs(report);
        Set<String> expected = Set.of("CVE-2020-9488", "CVE-2021-45046", "CVE-2021-45105", "CVE-2021-44228", "CVE-2021-44832");
        assertEquals(expected.size(),cves.size());
        assertEquals(expected,cves);
    }

    @Test
    public void testNotExisting() {
        assertThrows(IllegalArgumentException.class,() -> new SteadyResultAnalysis().getDetectedCVEs(Path.of("____foo")));
    }
}
