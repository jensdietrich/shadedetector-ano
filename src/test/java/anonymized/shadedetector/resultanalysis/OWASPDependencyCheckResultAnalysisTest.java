package anonymized.shadedetector.resultanalysis;

import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class OWASPDependencyCheckResultAnalysisTest {

    @Test
    public void testEmptyReport() {
        Path report = Path.of(OWASPDependencyCheckResultAnalysisTest.class.getResource("/sca/owasp_dc/owasp_dc-empty.json").getPath());
        Set<String> cves = new OWASPDependencyCheckResultAnalysis().getDetectedCVEs(report);
        assertTrue(cves.isEmpty());
    }

    @Test
    public void testNonEmptyReport() {
        Path report = Path.of(OWASPDependencyCheckResultAnalysisTest.class.getResource("/sca/owasp_dc/owasp_dc-nonempty.json").getPath());
        Set<String> cves = new OWASPDependencyCheckResultAnalysis().getDetectedCVEs(report);
        Set<String> expected = Set.of("CVE-2022-25857","CVE-2022-38749","CVE-2022-38752","CVE-2022-38750","CVE-2022-38751","CVE-2022-41854","CVE-2022-1471","CVE-2017-18640");
        assertEquals(expected.size(),cves.size());
        assertEquals(expected,cves);
    }

    @Test
    public void testNotExisting() {
        assertThrows(IllegalArgumentException.class,() -> new OWASPDependencyCheckResultAnalysis().getDetectedCVEs(Path.of("____foo")));
    }
}
