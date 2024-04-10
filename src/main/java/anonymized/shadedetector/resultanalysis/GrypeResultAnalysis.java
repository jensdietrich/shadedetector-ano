package anonymized.shadedetector.resultanalysis;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class GrypeResultAnalysis implements SCAResultAnalysis {

    @Override
    public Set<String> getDetectedCVEs(Path reportFile) {
        Preconditions.checkArgument(Files.exists(reportFile));
        Preconditions.checkArgument(!Files.isDirectory(reportFile));

        Set<String> vulnIds = new HashSet<>();
        try (Reader reader = Files.newBufferedReader(reportFile)) {
            JsonElement root = JsonParser.parseReader(reader);
            JsonArray matches = root.getAsJsonObject().get("matches").getAsJsonArray();
            for (JsonElement elem : matches) {
                // main vuln
                var vuln = elem.getAsJsonObject().getAsJsonObject("vulnerability");
                vulnIds.add(vuln.getAsJsonPrimitive("id").getAsString());
                // related
                var related = elem.getAsJsonObject().getAsJsonArray("relatedVulnerabilities");
                for (JsonElement relElem : related) {
                    vulnIds.add(relElem.getAsJsonObject().getAsJsonPrimitive("id").getAsString());
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Error parsing Grype report %s: %s", reportFile, e));
        }

        // Grype also uses GHSA IDs, we filter them out here
        return vulnIds.stream().filter(id -> id.startsWith("CVE-")).collect(Collectors.toSet());
    }
}
