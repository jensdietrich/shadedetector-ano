package anonymized.shadedetector;

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.URL;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class ArtifactSearchTest {


    private ArtifactSearchResponse response = null;
    private File json = null;
    private URL url = null;

    @BeforeEach
    public void setup() throws IOException {
        url = ArtifactSearchTest.class.getClassLoader().getResource("queryresult.json");
        System.out.println("reading test data from url " + url);
        json = new File(url.getFile());
        System.out.println("reading test data from file " + json.getAbsolutePath());
        try (Reader reader = new FileReader(json)) {
            response = ArtifactSearch.parse(reader);
        }
    }

    @AfterEach
    public void tearDown() {
        response = null;
        json = null;
        url = null;
    }

    @Test
    public void testResponseExists() {
        assertNotNull(response);
    }

    @Test
    public void testResponseHeader() {
        assumeTrue(response!=null);
        ResponseHeader header = response.getHeader();
        assertNotNull(header);
        assertEquals(0,header.getStatus());
        assertEquals(1467,header.getQtime());
        assertEquals(10,header.getParameters().getRows());
        assertEquals("score desc,timestamp desc,g asc,a asc,v desc",header.getParameters().getSort());
        assertEquals("2.2",header.getParameters().getVersion());
    }

    @Test
    public void testResponseBody() {
        assumeTrue(response!=null);
        ResponseBody body = response.getBody();
        assertNotNull(body);
        assertEquals(10714,body.getNumFound());
        assertEquals(0,body.getStart());
        assertNotNull(body.getArtifacts());
        assertEquals(10,body.getArtifacts().size());
    }

    private Artifact getArtifact(int idx) {
        assumeTrue(response!=null);
        ResponseBody body = response.getBody();
        assumeTrue(body!=null);
        List<Artifact> artifacts = body.getArtifacts();
        assumeTrue(artifacts!=null);
        assumeTrue(artifacts.size()==10);
        return artifacts.get(idx);
    }

    @Test
    public void testResponseArtifact1() {
        Artifact artifact = getArtifact(0);
        assertEquals("org.ops4j.pax.web:pax-web-spi:7.3.28",artifact.getId());
        assertEquals("org.ops4j.pax.web",artifact.getGroupId());
        assertEquals("pax-web-spi",artifact.getArtifactId());
        assertEquals("7.3.28",artifact.getVersion());
        assertEquals("bundle",artifact.getProjectType());
        assertEquals(1673251207000L,artifact.getTimestamp());
        assertEquals(List.of(
            "-sources.jar",
            ".pom",
            "-javadoc.jar",
            "-test-sources.jar",
            ".jar"),
            artifact.getResources());
        assertEquals(List.of(
            "based",
            "osgi",
            "found",
            "confluence",
            "detailed",
            "service",
            "wiki",
            "ops4j",
            "http",
            "information",
            "jetty",
            "ayaz"),
            artifact.getTags());
    }
}
