package anonymized.shadedetector.resultanalysis;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Extract reported CVEs from snyk reports.
 * @author jens dietrich
 */
public class SnykResultAnalysis implements SCAResultAnalysis {

    @Override
    public Set<String> getDetectedCVEs(Path snykReport) {
        Preconditions.checkArgument(Files.exists(snykReport));
        Preconditions.checkArgument(!Files.isDirectory(snykReport));

        Set<String> cves = new HashSet<>();
        try (Reader reader = Files.newBufferedReader(snykReport)) {
            JsonElement root = JsonParser.parseReader(reader);
            JsonArray vulnerabilities = root.getAsJsonObject().get("vulnerabilities").getAsJsonArray();
            for (int i=0;i<vulnerabilities.size();i++) {
                JsonObject nextVul = vulnerabilities.get(i).getAsJsonObject();
                JsonObject identifiers = nextVul.get("identifiers").getAsJsonObject();
                JsonArray ids = identifiers.get("CVE").getAsJsonArray();
                for (JsonElement id:ids) {
                    cves.add(id.getAsString());
                }
            }
            return cves;
        } catch (IOException e) {
            throw new IllegalArgumentException("error parsing JSON file - not a valid Snyk report: "+snykReport,e);
        }


    }
}
