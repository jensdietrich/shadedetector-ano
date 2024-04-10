package anonymized.shadedetector;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FetchResourceTest {

    // do it here instead of in @BeforeAll to avoid classloader issues
    static {
        Cache.setRoot(new File(".test-cache"));
    }

    private static Artifact apacheCommonsCollections4_40 = null;

    @BeforeAll
    public static void loadApacheCommonsCollections4_40() throws IOException {

        // reset cache to make sure actual resources are fetched and parsed
        Cache.clearCache(FetchResources.BIN_CACHE_NAME);
        Cache.clearCache(FetchResources.POM_CACHE_NAME);
        Cache.clearCache(FetchResources.SRC_CACHE_NAME);

        // load resources
        Path versionsPath = Path.of(FetchResourceTest.class.getResource("/artifacts/org.apache.commons!commons-collections4.json").getFile());
        assertTrue(Files.exists(versionsPath));
        try (Reader reader = Files.newBufferedReader(versionsPath)) {
            ArtifactSearchResponse response = ArtifactSearch.parse(reader);
            Optional<Artifact> result = response.getBody().getArtifacts().stream()
                    .filter(artifact -> artifact.getId().equals("org.apache.commons:commons-collections4:4.0"))
                    .findFirst();
            assertTrue(result.isPresent());
            apacheCommonsCollections4_40 = result.get();
        }
    }
    @AfterAll
    public static void releaseApacheCommonsCollections4_40() {
        apacheCommonsCollections4_40 = null;
    }

    @Test
    public void testBinCacheFolder() {
        assertTrue(FetchResources.BIN_CACHE.exists());
        assertEquals(new File(".test-cache/bin"),FetchResources.BIN_CACHE);
    }

    @Test
    public void testSrcCacheFolder() {
        assertTrue(FetchResources.SRC_CACHE.exists());
        assertEquals(new File(".test-cache/src"),FetchResources.SRC_CACHE);
    }

    @Test
    public void testPOMCacheFolder() {
        assertTrue(FetchResources.SRC_CACHE.exists());
        assertEquals(new File(".test-cache/pom"),FetchResources.POM_CACHE);
    }

    @Test
    public void testFetchBinForApacheCommonsCollections4_40() throws IOException {
        Path path = FetchResources.fetchBinaries(apacheCommonsCollections4_40);
        assertTrue(Files.exists(path));
    }

    @Test
    public void testFetchSrcForApacheCommonsCollections4_40() throws IOException {
        Path path = FetchResources.fetchSources(apacheCommonsCollections4_40);
        assertTrue(Files.exists(path));
    }

    @Test
    public void testFetchPOMForApacheCommonsCollections4_40() throws IOException {
        Path path = FetchResources.fetchPOM(apacheCommonsCollections4_40);
        assertTrue(Files.exists(path));
    }


}
