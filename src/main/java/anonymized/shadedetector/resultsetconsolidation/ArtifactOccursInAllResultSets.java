package anonymized.shadedetector.resultsetconsolidation;

import anonymized.shadedetector.Artifact;
import anonymized.shadedetector.ArtifactSearchResponse;
import anonymized.shadedetector.ArtifactSearchResultConsolidationStrategy;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Merge result sets for multiple queries using intersection.
 * I.e. an artifact will be investigated if it is in all result set (each result set corresponding to a class name).
 * @author jens dietrich
 */
public class ArtifactOccursInAllResultSets implements ArtifactSearchResultConsolidationStrategy {
    @Override
    public String name() {
        return "all";
    }

    @Override
    public List<Artifact> consolidate(Map<String, ArtifactSearchResponse> searchResults) {
        if (searchResults==null || searchResults.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        Map<Artifact,Integer> counter = countArtifactOccurrences(searchResults);
        return counter.keySet().stream()
            .filter(artifact -> counter.get(artifact)==searchResults.size())
            .collect(Collectors.toList());

    }
}
