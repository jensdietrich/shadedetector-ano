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


public class SteadyResultAnalysis implements SCAResultAnalysis {

    @Override
    public Set<String> getDetectedCVEs(Path steadyJsonReport) {
        Preconditions.checkArgument(Files.exists(steadyJsonReport));
        Preconditions.checkArgument(!Files.isDirectory(steadyJsonReport));

        Set<String> cves = new HashSet<>();
        try (Reader reader = Files.newBufferedReader(steadyJsonReport)) {
            JsonElement root = JsonParser.parseReader(reader);
            JsonArray vulnerabilities = root.getAsJsonObject().get("vulasReport").getAsJsonObject().get("vulnerabilities").getAsJsonArray();
            for (JsonElement vulnerability:vulnerabilities) {
                JsonObject bug = vulnerability.getAsJsonObject().getAsJsonObject("bug");
               String cve = bug.get("id").getAsString();
               cves.add(cve);
            }
            return cves;
        } catch (IOException e) {
            throw new IllegalArgumentException("error parsing JSON file - not a valid Steady report: "+steadyJsonReport,e);
        }


    }
}
