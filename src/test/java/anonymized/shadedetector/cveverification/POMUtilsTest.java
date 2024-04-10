package anonymized.shadedetector.cveverification;

import anonymized.shadedetector.GAV;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import static org.junit.jupiter.api.Assertions.*;

public class POMUtilsTest {

    private static final String ORIGINAL_POM_PATH = "/poms/io.github.jensdietrich.cve.fastjson-1.2.38_original.xml";
    private static final String MANIPULATED_POM_PATH = "/poms/io.github.jensdietrich.cve.fastjson-1.2.38_clone.xml";
    private Path originalPOM = null;
    private Path manipulatedDOM = null;


    @BeforeEach
    public void setup() throws IOException {
        originalPOM = Path.of(POMUtilsTest.class.getResource(ORIGINAL_POM_PATH).getPath());
        manipulatedDOM = Path.of(originalPOM.toString().replace(ORIGINAL_POM_PATH,MANIPULATED_POM_PATH));
        Assumptions.assumeTrue(Files.exists(originalPOM));
        Files.copy(originalPOM,manipulatedDOM, StandardCopyOption.REPLACE_EXISTING);
        Assumptions.assumeTrue(Files.exists(manipulatedDOM));
    }

    @AfterEach
    public void tearDown() throws IOException {
        if (Files.exists(manipulatedDOM)) {
            Files.delete(manipulatedDOM);
        }
    }

    @Test
    public void testDependencyExists() throws IOException, JDOMException {
        // the effective oracle is that this does not throw and exception
        Element dependency = POMUtils.findDependency(originalPOM,new GAV("com.alibaba","fastjson","1.2.80"));
        assertNotNull(dependency);
    }

    @Test
    public void testDependencyDoesNotExist1() throws IOException, JDOMException {
        Element dependency = POMUtils.findDependency(originalPOM,new GAV("com.alibaba2","fastjson","1.2.80"));
        assertNull(dependency);
    }

    @Test
    public void testDependencyDoesNotExist2() throws IOException, JDOMException {
        Element dependency = POMUtils.findDependency(originalPOM,new GAV("com.alibaba","fastjson2","1.2.80"));
        assertNull(dependency);
    }

    @Test
    public void testDependencyDoesNotExist3() throws IOException, JDOMException {
        Element dependency = POMUtils.findDependency(originalPOM,new GAV("com.alibaba","fastjson","1.2.81"));
        assertNull(dependency);
    }

    @Test
    public void testReplaceDependency() throws IOException, JDOMException {

        GAV originalGAV = new GAV("com.alibaba","fastjson","1.2.80");
        GAV replacementGAV = new GAV("com.foo","bar","1.2.3");

        Assumptions.assumeTrue(Files.exists(manipulatedDOM));
        Element dependency = POMUtils.findDependency(manipulatedDOM,originalGAV);
        Assumptions.assumeTrue(dependency!=null); // this is a fresh copy
        POMUtils.replaceDependency(manipulatedDOM,originalGAV,replacementGAV);

        Element updatedDependency = POMUtils.findDependency(manipulatedDOM,replacementGAV);
        Element removedDependency = POMUtils.findDependency(manipulatedDOM,originalGAV);

        assertNotNull(updatedDependency);
        assertNull(removedDependency);
    }

    @Test
    public void testChangeCoordinates() throws IOException, JDOMException {
        GAV oldCoordinates = new GAV("io.github.jensdietrich.cve.fastjson-1.2.38","original","1.0.0");
        GAV newCoordinates = new GAV("com.foo","bar","1.2.3");

        GAV extractedBefore = POMUtils.getCoordinates(manipulatedDOM);
        Assumptions.assumeTrue(extractedBefore!=null);
        Assumptions.assumeTrue(extractedBefore.equals(oldCoordinates));

        POMUtils.replaceCoordinates(manipulatedDOM,newCoordinates);

        GAV extractedAfter = POMUtils.getCoordinates(manipulatedDOM);
        assertNotNull(extractedAfter);
        assertEquals(extractedAfter,newCoordinates);

    }

}
