package anonymized.shadedetector.resultanalysis;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Extract reported CVEs from OWASP dependency check reports.
 * @author jens dietrich
 */
public class OWASPDependencyCheckResultAnalysis implements SCAResultAnalysis {

    @Override
    public Set<String> getDetectedCVEs(Path owaspDCReport) {
        Preconditions.checkArgument(Files.exists(owaspDCReport));
        Preconditions.checkArgument(!Files.isDirectory(owaspDCReport));

        Set<String> cves = new HashSet<>();
        try (Reader reader = Files.newBufferedReader(owaspDCReport)) {
            JsonElement root = JsonParser.parseReader(reader);
            JsonArray dependencies = root.getAsJsonObject().get("dependencies").getAsJsonArray();
            for (JsonElement dependency:dependencies) {
                if (!dependency.getAsJsonObject().has("vulnerabilities"))
                    continue;
                JsonArray vulnerabilities = dependency.getAsJsonObject().getAsJsonObject().get("vulnerabilities").getAsJsonArray();
                for (JsonElement vulnerability:vulnerabilities) {
                    String cve = vulnerability.getAsJsonObject().get("name").getAsString();
                    cves.add(cve);
                }
            }
            return cves;
        } catch (IOException e) {
            throw new IllegalArgumentException("error parsing JSON file - not a valid Snyk report: "+owaspDCReport,e);
        }


    }
}
