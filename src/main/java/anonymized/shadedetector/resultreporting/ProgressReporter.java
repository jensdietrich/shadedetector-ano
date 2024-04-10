package anonymized.shadedetector.resultreporting;

import anonymized.shadedetector.Artifact;
import anonymized.shadedetector.GAV;
import anonymized.shadedetector.ProcessingStage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility for reporting stats about the artifacts successfully processed at each state.
 * @author jens dietrich
 */
public class ProgressReporter {

    public static final String UNVERSIONED_KEY_EXTENSION = "_unversioned" ;

    private File output = null;

    private Map<ProcessingStage, Integer> versionedArtifactCounts = new LinkedHashMap<>();
    private Map<ProcessingStage, Integer> unversionedArtifactCounts = new LinkedHashMap<>();

    public ProgressReporter(File output) {
        this.output = output;
    }

    public File getOutput() {
        return output;
    }

    public void artifactsProcessed (ProcessingStage stage, Collection<Artifact> artifacts) {

        // remove potential duplicates and unnecessary info
        Set<GAV> versionedArtifactCoordinates = artifacts.stream()
            .map(a -> a.asGAV())
            .collect(Collectors.toSet());

        Set<GAV> unversionedArtifactCoordinates =
            artifacts.stream()
            .map(a -> a.asGAV())
            .map(gav -> new GAV(gav.getGroupId(),gav.getArtifactId(),null))
            .collect(Collectors.toSet());

        versionedArtifactCounts.put(stage,versionedArtifactCoordinates.size());
        unversionedArtifactCounts.put(stage,unversionedArtifactCoordinates.size());
    }

    public void endReporting() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(output))) {
            for (ProcessingStage stage: ProcessingStage.values()) {
                if (versionedArtifactCounts.containsKey(stage)) {
                    String key = stage.name();
                    writer.print(key);
                    writer.print("=");
                    writer.println(versionedArtifactCounts.get(stage));
                }
                if (unversionedArtifactCounts.containsKey(stage)) {
                    String key = stage.name();
                    writer.print(key + UNVERSIONED_KEY_EXTENSION);
                    writer.print("=");
                    writer.println(unversionedArtifactCounts.get(stage));
                }
            }
        }
    }


}
