package anonymized.shadedetector;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility to fetch resources associated with artifact.
 * Resources will be returned as files (java.io.File), referencing a location in the local cache.
 * @author jens dietrich
 */
public class FetchResources {

    private static Logger LOGGER = LoggerFactory.getLogger(FetchResources.class);

    static final String BIN_CACHE_NAME = "bin";
    static final String SRC_CACHE_NAME = "src";
    static final String POM_CACHE_NAME = "pom";

    static File BIN_CACHE = anonymized.shadedetector.Cache.getCache(BIN_CACHE_NAME);
    static File SRC_CACHE = anonymized.shadedetector.Cache.getCache(SRC_CACHE_NAME);
    static File POM_CACHE = Cache.getCache(POM_CACHE_NAME);

    public static final String SEARCH_URL = "https://search.maven.org/remotecontent";

    // for testing only
    // @TODO remove
    public static void main(String[] args) throws ArtifactSearchException {
        ArtifactSearchResponse artifactSearchResult = ArtifactSearch.findShadingArtifacts("InvokerTransformer",gav->true,5,200);
        AtomicInteger allCounter = new AtomicInteger();
        AtomicInteger fetchSuccessCounter = new AtomicInteger();
        AtomicInteger fetchSuccessButNotAZipCounter = new AtomicInteger();
        for (Artifact artifact:artifactSearchResult.getBody().getArtifacts()) {
            LOGGER.info("Looking for sources for artifact " + artifact.getId());
            allCounter.incrementAndGet();
            try {
                Path source = fetchSources(artifact);
                LOGGER.info("\tsource code fetched: " + source.toFile().getAbsolutePath());
                fetchSuccessCounter.incrementAndGet();
                if (!Utils.isZip(source.toFile())) {
                    LOGGER.warn("\tnot a zip file: " + source.toFile().getAbsolutePath());
                    fetchSuccessButNotAZipCounter.incrementAndGet();
                }
            }
            catch (Exception x) {
                LOGGER.error("\terror fetching sources for " + artifact.getId());
            }
        }

        LOGGER.info("artifacts processed: " + allCounter.get());
        LOGGER.info("fetch succeeded: " + fetchSuccessCounter.get());
        LOGGER.info("fetch succeeded but not a zip: " + fetchSuccessButNotAZipCounter.get());
    }

    public static Path fetchBinaries (Artifact artifact) throws IOException {
        GAV gav = new GAV(artifact.getGroupId(),artifact.getArtifactId(),artifact.getVersion());
        if (!artifact.getResources().contains(".jar")) {
            throw new IllegalStateException("no binaries found for artifact " + artifact.getId());
        }
        Path cached = getCachedBin(gav,"jar");
        return fetch(gav,cached,".jar");
    }

    public static Path fetchPOM (Artifact artifact) throws IOException {
        GAV gav = new GAV(artifact.getGroupId(),artifact.getArtifactId(),artifact.getVersion());
        if (!artifact.getResources().contains(".pom")) {
            throw new IllegalStateException("no POM found for artifact " + artifact.getId());
        }
        return fetchPOM(gav);
    }

    public static Path fetchPOM (GAV gav) throws IOException {
        Path cached = getCachedPOM(gav,".pom");
        return fetch(gav,cached,".pom");
    }

    public static Path fetchSources (Artifact artifact) throws IOException {
        GAV gav = new GAV(artifact.getGroupId(),artifact.getArtifactId(),artifact.getVersion());
        String sourceSuffix = artifact.getResources().stream()
            .filter(r -> r.contains("sources") || r.contains("src"))
            .filter(r -> r.endsWith(".jar") || r.endsWith(".zip"))  // important, otherwise hashes will be fetched sometimes
            //.orElse(null);
            .findFirst().orElse(null);
        if (sourceSuffix==null) {
            throw new IllegalStateException("no source code found for artifact " + artifact.getId());
        }

        Path cached = getCachedSrc(gav,sourceSuffix);
        return fetch(gav,cached,sourceSuffix);
    }

    private static Path fetch(GAV gav,Path cached,String suffix) throws IOException {
        // https://search.maven.org/remotecontent?filepath=com/jolira/guice/3.0.0/guice-3.0.0.pom
        if (Files.exists(cached)) {
            return cached;
        }
        HttpUrl.Builder urlBuilder = HttpUrl.parse(SEARCH_URL).newBuilder();
        String remotePath = gav.getGroupId().replace(".","/");
        remotePath = remotePath + '/' + gav.getArtifactId() + '/' + gav.getVersion() + '/' + gav.getArtifactId() + '-' + gav.getVersion();
        // urlBuilder.addQueryParameter("filepath", remotePath);
        // https://repo1.maven.org/maven2/com/iussoft/iussoft-api/3.0.1/iussoft-api-3.0.1-sources.jar
        String url = urlBuilder.build().toString();
        url = url + "?filepath=" + remotePath + suffix;
        return MvnRestAPIClient.fetchBinaryData(url,cached);
    }

    private static Path getCachedBin(GAV gav,String suffix) {
        return getCached(BIN_CACHE,gav,gav.getArtifactId()+"-"+gav.getVersion()+suffix);
    }

    private static Path getCachedPOM(GAV gav,String suffix) {
        return getCached(POM_CACHE,gav,gav.getArtifactId()+"-"+gav.getVersion()+suffix);
    }

    private static Path getCachedSrc(GAV gav,String suffix) {
        return getCached(SRC_CACHE,gav,gav.getArtifactId()+"-"+gav.getVersion()+suffix);
    }

    // @TODO can we use name given by project -- need to check whether query results return it
    private static Path getCached(File cacheRoot, GAV gav, String fileName) {
        File groupFolder = new File(cacheRoot,gav.getGroupId());
        File artifactFolder = new File(groupFolder,gav.getArtifactId());
        File versionFolder = new File(artifactFolder,gav.getVersion());
        if (!versionFolder.exists()) {
            versionFolder.mkdirs();
        }

        return new File(versionFolder,fileName).toPath();
    }
}
