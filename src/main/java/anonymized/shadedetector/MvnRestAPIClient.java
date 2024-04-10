package anonymized.shadedetector;

import com.google.common.io.ByteStreams;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.TimeUnit;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiConsumer;

/**
 * Utility to connect to the Mvn Rest API with a feature to retry if connections fail.
 * @author jens dietrich
 */
public class MvnRestAPIClient {

    private static Logger LOGGER = LoggerFactory.getLogger(MvnRestAPIClient.class);
    private static final int MAX_RETRY_ATTEMPTS = 3;

    private static BiConsumer<Response,Path> BINARY_CACHING = (response,cached) -> {
        try (InputStream in = response.body().byteStream(); OutputStream out = Files.newOutputStream(cached)) {
            ByteStreams.copy(in,out);
        } catch (IOException e) {
            LOGGER.error("problem caching response in {}",cached.toString(),e);
            throw new RuntimeException(e);
        }
    };

    private static BiConsumer<Response,Path> CHAR_CACHING = (response,cached) -> {
        Utils.cacheCharsToPath(response.body().charStream(), cached);
    };

    public static Path fetchBinaryData (String url, Path cached) throws IOException {
        return fetch (url, cached, BINARY_CACHING,1);
    }

    public static Path fetchCharData (String url, Path cached) throws IOException {
        return fetch (url, cached, CHAR_CACHING,1);
    }

    private static Path fetch (String url, Path cached, BiConsumer<Response,Path> cacheAction,int attempt) throws IOException {

        if (Files.exists(cached)) {
            LOGGER.info("using cached data from " + cached.toFile().getAbsolutePath());
            return cached;
        }

        // The default timeout of 10s is often much too short for "c:..." queries to https://search.maven.org/solrsearch/select
        OkHttpClient client = new OkHttpClient.Builder().readTimeout(60, TimeUnit.SECONDS).build();
        LOGGER.info("fetching data (attempt {}): GET {}",attempt,url);
        Request request = new Request.Builder().url(url).build();
        Call call = client.newCall(request);
        Response response = null;

        try {
            response = call.execute();
        }
        catch (IOException x) {
            if (attempt < MAX_RETRY_ATTEMPTS) {
                return fetch(url,cached,cacheAction,attempt+1);
            }
            throw new IOException("failed to download resource from " + url,x);
        }

        int responseCode = response.code();
        LOGGER.info("\tresponse code is: " + responseCode);

        if (responseCode==200) {
            LOGGER.info("\tcaching data in " + cached.toFile().getAbsolutePath());
            cacheAction.accept(response,cached);
        }
        else {
            if (attempt < MAX_RETRY_ATTEMPTS) {
                return fetch(url,cached,cacheAction,attempt+1);
            } else {
                throw new IOException("failed to download resource from " + url + " , response code: " + responseCode + " - " + response.message());
            }
        }

        assert Files.exists(cached);
        return cached;
    }
}
