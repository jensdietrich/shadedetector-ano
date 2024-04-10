package anonymized.shadedetector.resultreporting;

import anonymized.shadedetector.Artifact;
import anonymized.shadedetector.CloneDetector;
import anonymized.shadedetector.ResultReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Reporting based on CSV, will report a separate file for each pair of artifacts analysed.
 * @author jens dietrich
 */
public class CSVDetailedResultReporter implements ResultReporter {

    public static final String SEP = "\t";
    private static Logger LOGGER = LoggerFactory.getLogger(CSVDetailedResultReporter.class);

    public static final String[] COLUMNS = new String[] {
        "original-artifact",
        "cloning-artifact",
        "original-classfile",
        "clones-classfile",
        "similarity score",
        "verification-project-state",
        "packages names changed"
    };

    // the destination folder, a separate file will be created for each artifact
    private String dir = "out";

    public CSVDetailedResultReporter(String dir) {
        this.dir = dir;
    }

    public CSVDetailedResultReporter() {

    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dest) {
        this.dir = dest;
    }

    @Override
    public String name() {
        return "csv.details";
    }

    @Override
    public void report(Artifact component, Artifact potentialClone, List<Path> potentialCloneSources, Set<CloneDetector.CloneRecord> cloneAnalysesResults, ResultReporter.VerificationState state, boolean packagesHaveChangedInClone) throws IOException {
        String header = Stream.of(COLUMNS).collect(Collectors.joining(SEP));
        List<String> rows = new ArrayList<>();
        rows.add(header);

        for (CloneDetector.CloneRecord record:cloneAnalysesResults) {
            String row = "" + component.getId() + SEP + potentialClone.getId() + SEP + record.getOriginal() + SEP + record.getClone() + SEP + record.getConfidence() + SEP + state.name() + SEP + packagesHaveChangedInClone;
            rows.add(row);
        }

        Path folder = Path.of(dir);
        if (Files.notExists(folder)) {
            Files.createDirectories(folder);
        }

        Path report = Paths.get(dir, potentialClone.getId()+".csv");
        LOGGER.info("Reporting to " + report.toFile().getAbsolutePath());
        Files.write(report,rows);
    }

    @Override
    public void startReporting(Artifact component, Path sources) {}

    @Override
    public void endReporting(Artifact component) {}
}
