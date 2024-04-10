package anonymized.shadedetector.clonedetection;

import anonymized.shadedetector.CloneDetector;
import anonymized.shadedetector.Utils;
import com.github.javaparser.utils.Pair;
import com.google.common.base.Preconditions;
import anonymized.shadedetector.cveverification.ASTUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * An utility that given some clone records, extracts a function to translates imports,
 * which can then be used to clone projects supporting shading.
 * @author jens dietrich
 */
public class ImportTranslationExtractor {

    private static Logger LOGGER = LoggerFactory.getLogger(ImportTranslationExtractor.class);
    public  static final double DEFAULT_SIMILARITY_THREASHOLD = 0.9;

    /**
     * Extract the imported types translation table using the default similarity threshold.
     * @param cloneRecords a set of clone records
     * @return
     */
    public static Map<String,String> computeImportTranslations (Path originalSources, Path clonedSources, Set<CloneDetector.CloneRecord> cloneRecords) throws IOException {
        return computeImportTranslations(originalSources,clonedSources,cloneRecords,DEFAULT_SIMILARITY_THREASHOLD);
    }

    /**
     * Extract the imported types translation table.
     * @param cloneRecords a set of clone records
     * @param threshold the similarity threshold (must be <=0 and <=1)
     * @return
     */
    public static Map<String,String> computeImportTranslations (Path originalSources, Path clonedSources,Set<CloneDetector.CloneRecord> cloneRecords, double threshold) throws IOException {
        Preconditions.checkArgument(threshold>=0d);
        Preconditions.checkArgument(threshold<=1d);

        Map<String,String> map = new HashMap<>();

        List<Path> originalJavaSources = Utils.listJavaSources(originalSources,true);
        List<Path> cloneCandidateJavaSources = Utils.listJavaSources(clonedSources,true);

        List<Pair<Path,Path>> records = new ArrayList<>();
        for (Path originalSource:originalJavaSources) {
            String cuName1 = originalSource.getName(originalSource.getNameCount()-1).toString();
            // skip package-info.java with package meta data
            if (!cuName1.equals("package-info.java")) {
                for (Path cloneSource : cloneCandidateJavaSources) {
                    String cuName2 = cloneSource.getName(cloneSource.getNameCount() - 1).toString();
                    if (Objects.equals(cuName1, cuName2)) {
                        records.add(new Pair<>(originalSource, cloneSource));
                    }
                }
            }
        }
        for (Pair<Path,Path> record:records) {
            String name1 = ASTUtils.getFullyQualifiedClassname(record.a);
            String name2 = ASTUtils.getFullyQualifiedClassname(record.b);
            if (!Objects.equals(name1,name2)) {
                map.put(name1,name2);
            }
        }

        // clone records will override (in the unlikely case that there are multiple matches this might correct
        for (CloneDetector.CloneRecord record:cloneRecords) {
            if (record.getConfidence() >= threshold) {
                try {
                    String name1 = ASTUtils.getFullyQualifiedClassname(record.getOriginal());
                    String name2 = ASTUtils.getFullyQualifiedClassname(record.getClone());
                    if (!Objects.equals(name1,name2)) {
                        map.put(name1,name2);
                    }
                }
                catch (Exception x) {
                    LOGGER.error("error extracting and comparing class names frpm {} and {}",record.getOriginal(),record.getClone(),x);
                }
            }
        }

        return map;
    }
}
