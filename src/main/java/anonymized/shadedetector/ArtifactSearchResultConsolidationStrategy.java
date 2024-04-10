package anonymized.shadedetector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Given search results by class name used by query, select the ones to proceed with by some heuristics.
 * Example: merge sets, use intersection, select all that occur in at least two result sets, etc.
 * @author jens dietrich
 */
public interface ArtifactSearchResultConsolidationStrategy  extends NamedService {

    List<Artifact> consolidate (Map<String,ArtifactSearchResponse> searchResults);

    default Map<Artifact,Integer> countArtifactOccurrences (Map<String, ArtifactSearchResponse> searchResults) {
        Map<Artifact,Integer> counter = new HashMap<>();
        searchResults.values().stream()
            .flatMap(result -> result.getBody().getArtifacts().stream())
            .forEach(artifact -> {
                if (counter.containsKey(artifact)) {
                    counter.put(artifact,counter.get(artifact)+1);
                }
                else {
                    counter.put(artifact,1);
                }
            });
        return counter;
    }
}
