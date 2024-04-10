package anonymized.shadedetector.resultreporting;

import anonymized.shadedetector.Artifact;
import anonymized.shadedetector.CloneDetector;
import anonymized.shadedetector.ResultReporter;
import anonymized.shadedetector.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Set;
import java.util.function.DoublePredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Reporting based on CSV, reporting only a summary for all artifact pairs compared in a single file.
 * @author jens dietrich
 */
public class CSVSummaryResultReporter implements ResultReporter {

    public static final String SEP = "\t";
    private static Logger LOGGER = LoggerFactory.getLogger(CSVSummaryResultReporter.class);

    public static final String[] COLUMNS = new String[] {
        "original-artifact",
        "cloning-artifact",
        "class-count-original",
        "class-count-clone",
        "matched-original-sources-all",
        "conf-(0.9-1.0]",
        "conf-(0.8-0.9]",
        "conf-(0.7-0.8]",
        "conf-(0.6-0.7]",
        "conf-(0.5-0.6]",
        "conf-(0.4-0.5]",
        "conf-(0.3-0.6]",
        "conf-(0.2-0.3]",
        "conf-(0.1-0.2]",
        "conf-(0.0-0.1]",
        "conf-0.0",
        "verification-compiled",
        "verification-tested",
        "packages changed"
    };

    private String file = "summary.csv";
    private List<Path> originalSources = null;

    public CSVSummaryResultReporter(String file) {
        this.file = file;
    }

    public CSVSummaryResultReporter() {}

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    @Override
    public String name() {
        return "csv.summary";
    }

    @Override
    public void report(Artifact component, Artifact potentialClone, List<Path> potentialCloneSources, Set<CloneDetector.CloneRecord> cloneAnalysesResults, ResultReporter.VerificationState state, boolean packagesHaveChangedInClone) throws IOException {

        String row = "";
        row = row + component.getId() + SEP;
        row = row + potentialClone.getId() + SEP;
        row = row + originalSources.size() + SEP;
        row = row + potentialCloneSources.size() + SEP;

        long matchCount = originalSources.stream().filter(path -> hasCloneRecord(path,cloneAnalysesResults)).count();
        row = row + matchCount + SEP;

        row = row + count(cloneAnalysesResults, c -> c>0.9 && c<=1.0) + SEP;
        row = row + count(cloneAnalysesResults, c -> c>0.8 && c<=0.9) + SEP;
        row = row + count(cloneAnalysesResults, c -> c>0.7 && c<=0.8) + SEP;
        row = row + count(cloneAnalysesResults, c -> c>0.6 && c<=0.7) + SEP;
        row = row + count(cloneAnalysesResults, c -> c>0.5 && c<=0.6) + SEP;
        row = row + count(cloneAnalysesResults, c -> c>0.4 && c<=0.5) + SEP;
        row = row + count(cloneAnalysesResults, c -> c>0.3 && c<=0.4) + SEP;
        row = row + count(cloneAnalysesResults, c -> c>0.2 && c<=0.3) + SEP;
        row = row + count(cloneAnalysesResults, c -> c>0.1 && c<=0.2) + SEP;
        row = row + count(cloneAnalysesResults, c -> c>0.0 && c<=0.1) + SEP;
        row = row + count(cloneAnalysesResults, c -> c==0.0) + SEP;
        row = row + (state==VerificationState.COMPILED?1:0) + SEP;
        row = row + (state==VerificationState.TESTED?1:0) + SEP;
        row = row + (packagesHaveChangedInClone?1:0);

        Files.write(new File(file).toPath(),List.of(row), StandardOpenOption.APPEND);

    }

    private long count(Set<CloneDetector.CloneRecord> cloneAnalysesResults, DoublePredicate constraint) {
        return cloneAnalysesResults.stream().mapToDouble(r -> r.getConfidence()).filter(constraint).count();
    }

    private boolean hasCloneRecord(Path p,Set<CloneDetector.CloneRecord> cloneRecords) {
        for (CloneDetector.CloneRecord record:cloneRecords) {
            if (record.getOriginal().equals(p)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void startReporting(Artifact component, Path sources) throws IOException {
        File aFile = new File(file);
//        if (aFile.getParentFile()==null || !aFile.getParentFile().exists()) {
//            aFile.mkdirs();
//        }

        LOGGER.info("Reporting to " + aFile.getAbsolutePath());

        String header = Stream.of(COLUMNS).collect(Collectors.joining(SEP));
        Files.write(aFile.toPath(),List.of(header), StandardOpenOption.TRUNCATE_EXISTING,StandardOpenOption.WRITE,StandardOpenOption.CREATE);

        originalSources = Utils.listJavaSources(sources,true);
    }

    @Override
    public void endReporting(Artifact component) throws IOException {
        originalSources = null;
    }
}
