package anonymized.shadedetector;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * Interface describing how analysis results are being consumed / reported.
 * @author jens dietrich
 */
public interface ResultReporter extends NamedService {

    enum VerificationState {NONE, COMPILED, TESTED};

    void report (Artifact component, Artifact potentialClone, List<Path> potentialCloneSources, Set<CloneDetector.CloneRecord> cloneAnalysesResults, VerificationState verificationState, boolean packagesHaveChangedInClone) throws IOException;

    void startReporting (Artifact component, Path sources) throws IOException;
    void endReporting (Artifact component) throws IOException;
}
