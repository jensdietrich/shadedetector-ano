package anonymized.shadedetector.resultsetconsolidation;

import anonymized.shadedetector.Artifact;
import anonymized.shadedetector.ArtifactSearchResponse;
import anonymized.shadedetector.ArtifactSearchResultConsolidationStrategy;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Merge result sets for multiple queries using union.
 * I.e. an artifact will be investigated if it is in any result set (each result set corresponding to a class name).
 * @author jens dietrich
 */
public class ArtifactOccursInAnyResultSet implements ArtifactSearchResultConsolidationStrategy {
    @Override
    public String name() {
        return "any";
    }

    @Override
    public List<Artifact> consolidate(Map<String, ArtifactSearchResponse> searchResults) {
        if (searchResults==null || searchResults.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        return searchResults.values().stream()
            .flatMap(result -> result.getBody().getArtifacts().stream())
            .collect(Collectors.toList());
    }
}
