package anonymized.shadedetector;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class ArtifactSearchResponseMergerTest {
    private ArtifactSearchResponse response1 = null;
    private ArtifactSearchResponse response2 = null;

    @BeforeEach
    public void setup() throws IOException {
        response1 = loadJsonFromResource("queryresult.json");   //NOTE: Also used by ArtifactSearchTest
        response2 = loadJsonFromResource("queryresult2.json");
    }

    private ArtifactSearchResponse loadJsonFromResource(String filename) throws IOException {
        URL url = ArtifactSearchTest.class.getClassLoader().getResource(filename);
        System.out.println("reading test data from url " + url);
        File json = new File(url.getFile());
        System.out.println("reading test data from file " + json.getAbsolutePath());
        try (Reader reader = new FileReader(json)) {
            return ArtifactSearch.parse(reader);
        }
    }

    @AfterEach
    public void tearDown() {
        response1 = null;
        response2 = null;
    }

    @Test
    public void testResponsesExist() {
        assertNotNull(response1);
        assertNotNull(response2);
    }

    @Test
    public void testMerge() {
        assumeTrue(response1 != null);
        assumeTrue(response2 != null);
        ArtifactSearchResponse merged = ArtifactSearchResponseMerger.merge(List.of(response1, response2));
        assertEquals(15, merged.getBody().getArtifacts().size());   // 10+5
        assertEquals(15, merged.getHeader().getParameters().getRows());   // 10+5
        assertEquals(10714, merged.getBody().getNumFound());        // Does not sum
    }
}
