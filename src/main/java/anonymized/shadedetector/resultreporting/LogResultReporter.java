package anonymized.shadedetector.resultreporting;

import anonymized.shadedetector.Artifact;
import anonymized.shadedetector.CloneDetector;
import anonymized.shadedetector.ResultReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * Simple reporting using a logger.
 * @author jens dietrich
 */
public class LogResultReporter implements ResultReporter {

    private static Logger LOGGER = LoggerFactory.getLogger(LogResultReporter.class);

    public LogResultReporter() {
    }

    @Override
    public String name() {
        return "log";
    }

    @Override
    public void report(Artifact component, Artifact potentialClone, List<Path> potentialCloneSpources, Set<CloneDetector.CloneRecord> cloneAnalysesResults, VerificationState state, boolean packagesHaveChangedInClone) throws IOException {
        for (CloneDetector.CloneRecord record:cloneAnalysesResults) {
            LOGGER.info("Potential clone");
            LOGGER.info("\tcomponent: {}", component.getId());
            LOGGER.info("\tcloning-component: {}", potentialClone.getId());
            LOGGER.info("\toriginal-class: {}", record.getOriginal().toString());
            LOGGER.info("\tcloned-class: {}", record.getClone().toString());
            LOGGER.info("\tconfidence: {}", record.getConfidence());
            LOGGER.info("\tverification state: {}", state.name());
            LOGGER.info("\tpackages changed: {}",packagesHaveChangedInClone);
        }
    }

    @Override
    public void startReporting(Artifact component, Path sources) throws IOException {
        LOGGER.info("Start result reporting");
    }

    @Override
    public void endReporting(Artifact component) throws IOException {
        LOGGER.info("Finish result reporting");
    }
}
