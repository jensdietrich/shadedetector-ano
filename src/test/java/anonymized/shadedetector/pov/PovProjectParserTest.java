package anonymized.shadedetector.pov;

import anonymized.shadedetector.cveverification.TestSignal;
import anonymized.shadedetector.resultanalysis.SnykResultAnalysisTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PovProjectParserTest {

    private static File json = null;

    @BeforeAll
    public static void init() {
        json = Path.of(SnykResultAnalysisTest.class.getResource("/pov/pov-project.json").getPath()).toFile();
    }

    @AfterAll
    public static void tearDown() {
        json = null;
    }

    @Test
    public void test() throws FileNotFoundException {
        PovProject pov = PovProjectParser.parse(json);
        Assumptions.assumeTrue(json.exists());


//        {
//            "id": "CVE-2018-1324",
//            "artifact": "org.apache.commons:commons-compress",
//            "vulnerableVersions": [
//            "1.11",
//                "1.12",
//                "1.13",
//                "1.14",
//                "1.15"
//            ],
//            "fixVersion": "1.16.1",
//            "testSignal": "success",
//            "references": [
//            "https://nvd.nist.gov/vuln/detail/CVE-2018-1324",
//                "https://github.com/advisories/GHSA-h436-432x-8fvx"
//             ]
//        }

        assertEquals("CVE-2018-1324",pov.getId());
        assertEquals("org.apache.commons:commons-compress",pov.getArtifact());
        assertEquals("1.16.1",pov.getFixVersion());
        assertEquals(TestSignal.SUCCESS,pov.getTestSignalWhenVulnerable());

        assertEquals(List.of("1.11","1.12","1.13","1.14","1.15"),pov.getVulnerableVersions());
        assertEquals(List.of("https://nvd.nist.gov/vuln/detail/CVE-2018-1324","https://github.com/advisories/GHSA-h436-432x-8fvx"),pov.getReferences());
    }
}
