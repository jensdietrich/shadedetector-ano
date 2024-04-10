package anonymized.shadedetector;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import java.io.File;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class POMAnalysisTest {


    @Test @Disabled  // TODO semantics of function has changed, must refactor
    public void testShadePluginIncludeWithArtifactWildcard() throws Exception {
        File pom = new File(POMAnalysisTest.class.getResource("/poms/common-test-146.pom").getFile());
        Assumptions.assumeTrue(pom.exists());
        assertTrue(POMAnalysis.shadePluginReferences(pom.toPath(),"org.apache.commons","commons-collections4"));
    }

    @Test
    public void testShadePluginIncludeWithFullArtifactName() throws Exception {
        File pom = new File(POMAnalysisTest.class.getResource("/poms/common-test-146.pom").getFile());
        Assumptions.assumeTrue(pom.exists());
        assertTrue(POMAnalysis.shadePluginReferences(pom.toPath(),"com.edropple.jregex","jregex"));
    }

    @Test
    public void testShadePluginIncludeWithFullArtifactNameNeg() throws Exception {
        File pom = new File(POMAnalysisTest.class.getResource("/poms/common-test-146.pom").getFile());
        Assumptions.assumeTrue(pom.exists());
        assertFalse(POMAnalysis.shadePluginReferences(pom.toPath(),"com.edropple.jregex","foo"));
    }

    @Test @Disabled  // TODO semantics of function has changed, must refactor
    public void testShadePluginIncludeWithGroupPrefix1() throws Exception {
        File pom = new File(POMAnalysisTest.class.getResource("/poms/jmx_prometheus_javaagent-0.3.0.pom").getFile());
        Assumptions.assumeTrue(pom.exists());
        assertTrue(POMAnalysis.shadePluginReferences(pom.toPath(),"org.yaml","snakeyaml"));
    }

    @Test @Disabled  // TODO semantics of function has changed, must refactor
    public void testShadePluginIncludeWithGroupPrefix2() throws Exception {
        File pom = new File(POMAnalysisTest.class.getResource("/poms/jmx_prometheus_javaagent-0.3.0.pom").getFile());
        Assumptions.assumeTrue(pom.exists());
        assertTrue(POMAnalysis.shadePluginReferences(pom.toPath(),"org.yaml.foo","bar"));
    }

    @Test
    public void testShadePluginIncludeWithGroupPrefixNeg1() throws Exception {
        File pom = new File(POMAnalysisTest.class.getResource("/poms/jmx_prometheus_javaagent-0.3.0.pom").getFile());
        Assumptions.assumeTrue(pom.exists());
        assertFalse(POMAnalysis.shadePluginReferences(pom.toPath(),"org.yaaml.foo","bar"));
    }

    @Test
    public void testGAVMatch1() throws Exception {
        File pom = new File(POMAnalysisTest.class.getResource("/poms/jmx_prometheus_javaagent-0.3.0.pom").getFile());
        Assumptions.assumeTrue(pom.exists());
        assertTrue(POMAnalysis.hasGroupAndArtifactId(pom.toPath(),"io.prometheus.jmx","jmx_prometheus_javaagent"));
    }

    @Test
    public void testGAVNonMatch1() throws Exception {
        File pom = new File(POMAnalysisTest.class.getResource("/poms/jmx_prometheus_javaagent-0.3.0.pom").getFile());
        Assumptions.assumeTrue(pom.exists());
        assertFalse(POMAnalysis.hasGroupAndArtifactId(pom.toPath(),"foo","jmx_prometheus_javaagent"));
    }

    @Test
    public void testGAVNonMatch2() throws Exception {
        File pom = new File(POMAnalysisTest.class.getResource("/poms/jmx_prometheus_javaagent-0.3.0.pom").getFile());
        Assumptions.assumeTrue(pom.exists());
        assertFalse(POMAnalysis.hasGroupAndArtifactId(pom.toPath(),"io.prometheus.jmx","foo"));
    }
}
