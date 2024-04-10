package anonymized.shadedetector.resultanalysis;

import anonymized.shadedetector.ProcessingStage;
import com.google.common.base.Preconditions;
import anonymized.shadedetector.resultreporting.ProgressReporter;
import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility to report pipeline throughput
 * @author jens dietrich
 */
public class PipelineAnalysis {

    // example: stats100-CVE-2013-2186.log
    public static final Pattern SUMMARY_FILE_PATTERN = Pattern.compile("stats.+-CVE-\\d\\d\\d\\d-\\d*\\.log");

    public static final String CSV_SEPARATOR = "\t";

    // extract CVE from filename
    public static final Function<File,String> FILE2CVE = f -> {
        String name = f.getName().replace(".log","");
        return name.substring(name.indexOf("-")+1);
    };


    public static void main (String[] args) throws IOException {

        Preconditions.checkArgument(args.length==3,"three arguments required - the result folder containing summary reports, and the latex output file name and the csv output file name");
        File SUMMARY_FOLDER = new File(args[0]);
        Preconditions.checkArgument(SUMMARY_FOLDER.exists(),"folder does not exist: " + SUMMARY_FOLDER.getAbsolutePath());
        File latexReport = new File(args[1]);
        File csvReport = new File(args[2]);

        System.out.println("creating summary report (latex) in " + latexReport.getAbsolutePath());
        System.out.println("creating summary report (csv) in " + csvReport.getAbsolutePath());

        // not the greatest design to write those simultaneously but gets the job done (over-design is an anitpattern :-) )
        try (
            PrintWriter latexOut = new PrintWriter(new FileWriter(latexReport));
            PrintWriter csvOut = new PrintWriter(new FileWriter(csvReport))
        ) {

            // latex header
            latexOut.println("\\begin{table*}");
            latexOut.println("\t\\begin{tabular}{|l|p{2cm}p{2cm}p{2cm}p{2cm}p{2cm}p{2cm}|}");
            latexOut.println("\t\\hline");
            latexOut.println(asLatexTableRow(
    "vulnerability",
                "query results",
                "consolidated",
                "pom valid",
                "no dependency",
                // "sources acquired",
                "sources acquired",
                "clones detected",
                "pov compilable",
                "pov testable",
                "vulnerability confirmed",
                "vulnerability shaded"
            ));
            latexOut.println("\t\\hline");

            // csv header -- human friendly
//            csvOut.println(asCSVRow(
//                "vulnerability",
//                "query results (versions)",
//                "query results (unversioned)",
//                "consolidated (versions)",
//                "consolidated (unversioned)",
//                "no dependency (versions)",
//                "no dependency (unversioned)",
//                "clones detected (versions)",
//                "clones detected (unversioned)",
//                "pov compiled (versions)",
//                "pov compiled (unversioned)",
//                "pov tested (versions)",
//                "pov tested (unversioned)"
//            ));

            // csv header -- R friendly
            csvOut.println(asCSVRow(
                "vulnerability",
                "queryresults",
                "queryresults-agg",
                "consolidated",
                "consolidated-agg",
                "validpom",
                "validpom-agg",
                "nodependency",
                "nodependency-agg",
//                "sourcesfetched",
//                "sourcesfetched-agg",
                "sourcesacquired",
                "sourcesacquired-agg",
                "clonesdetected",
                "clonesdetected-agg",
                "pov-compilable",
                "pov-compilable-agg",
                "pov-testable",
                "pov-testable-agg",
                "vulconfirmed",
                "vulconfirmed-agg",
                "shaded",
                "shaded-agg"
            ));

            latexOut.println("\t\\hline");


            // BODY of table

            // counters for summary in last row
            Map<ProcessingStage,Integer> versionedCounters = new HashMap<>();
            Map<ProcessingStage,Integer> unversionedCounters = new HashMap<>();

            Stream.of(SUMMARY_FOLDER.listFiles())
                .sorted()
                .filter(f -> SUMMARY_FILE_PATTERN.matcher(f.getName()).matches())
                .forEach(f -> {
                    Properties properties = new Properties();
                    try (Reader reader = new FileReader(f)) {
                        properties.load(reader);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    aggregate(properties, ProcessingStage.QUERY_RESULTS,versionedCounters,unversionedCounters);
                    aggregate(properties, ProcessingStage.CONSOLIDATED_QUERY_RESULTS,versionedCounters,unversionedCounters);
                    aggregate(properties, ProcessingStage.VALID_POM,versionedCounters,unversionedCounters); // new
                    aggregate(properties, ProcessingStage.NO_DEPENDENCY_TO_VULNERABLE,versionedCounters,unversionedCounters);
//                    aggregate(properties, ProcessingStage.SOURCES_FETCHED,versionedCounters,unversionedCounters); // new
                    aggregate(properties, ProcessingStage.SOURCES_EXPANDED,versionedCounters,unversionedCounters); // new
                    aggregate(properties, ProcessingStage.CLONE_DETECTED,versionedCounters,unversionedCounters);
                    aggregate(properties, ProcessingStage.POV_INSTANCE_COMPILED,versionedCounters,unversionedCounters);
                    aggregate(properties, ProcessingStage.POV_INSTANCE_SUREFIRE_REPORTS_GENERATED,versionedCounters,unversionedCounters); // new
                    aggregate(properties, ProcessingStage.POV_INSTANCE_VULNERABILITY_CONFIRMED,versionedCounters,unversionedCounters);
                    aggregate(properties, ProcessingStage.POV_INSTANCE_VULNERABILITY_CONFIRMED_SHADED,versionedCounters,unversionedCounters);

                    latexOut.println(asLatexTableRow(
                        FILE2CVE.apply(f),
                        getLatexValue(properties, ProcessingStage.QUERY_RESULTS),
                        getLatexValue(properties, ProcessingStage.CONSOLIDATED_QUERY_RESULTS),
                        getLatexValue(properties, ProcessingStage.VALID_POM),
                        getLatexValue(properties, ProcessingStage.NO_DEPENDENCY_TO_VULNERABLE),
//                        getLatexValue(properties, ProcessingStage.SOURCES_FETCHED),
                        getLatexValue(properties, ProcessingStage.SOURCES_EXPANDED),
                        getLatexValue(properties, ProcessingStage.CLONE_DETECTED),
                        getLatexValue(properties, ProcessingStage.POV_INSTANCE_COMPILED),
                        getLatexValue(properties, ProcessingStage.POV_INSTANCE_SUREFIRE_REPORTS_GENERATED),
                        getLatexValue(properties, ProcessingStage.POV_INSTANCE_VULNERABILITY_CONFIRMED),
                        getLatexValue(properties, ProcessingStage.POV_INSTANCE_VULNERABILITY_CONFIRMED_SHADED)
                    ));

                    csvOut.println(asCSVRow(
                        FILE2CVE.apply(f),
                        getCSVValue(properties, ProcessingStage.QUERY_RESULTS),
                        getCSVValue(properties, ProcessingStage.CONSOLIDATED_QUERY_RESULTS),
                        getCSVValue(properties, ProcessingStage.VALID_POM),
                        getCSVValue(properties, ProcessingStage.NO_DEPENDENCY_TO_VULNERABLE),
//                        getCSVValue(properties, ProcessingStage.SOURCES_FETCHED),
                        getCSVValue(properties, ProcessingStage.SOURCES_EXPANDED),
                        getCSVValue(properties, ProcessingStage.CLONE_DETECTED),
                        getCSVValue(properties, ProcessingStage.POV_INSTANCE_COMPILED),
                        getCSVValue(properties, ProcessingStage.POV_INSTANCE_SUREFIRE_REPORTS_GENERATED),
                        getCSVValue(properties, ProcessingStage.POV_INSTANCE_VULNERABILITY_CONFIRMED),
                        getCSVValue(properties, ProcessingStage.POV_INSTANCE_VULNERABILITY_CONFIRMED_SHADED)
                    ));
                });

            latexOut.println("\t\\hline");

            latexOut.println(asLatexTableRow(
                    "(sum)",
                    getAggregatedValue(ProcessingStage.QUERY_RESULTS,versionedCounters,unversionedCounters),
                    getAggregatedValue(ProcessingStage.CONSOLIDATED_QUERY_RESULTS,versionedCounters,unversionedCounters),
                    getAggregatedValue(ProcessingStage.VALID_POM,versionedCounters,unversionedCounters),
                    getAggregatedValue(ProcessingStage.NO_DEPENDENCY_TO_VULNERABLE,versionedCounters,unversionedCounters),
//                    getAggregatedValue(ProcessingStage.SOURCES_FETCHED,versionedCounters,unversionedCounters),
                    getAggregatedValue(ProcessingStage.SOURCES_EXPANDED,versionedCounters,unversionedCounters),
                    getAggregatedValue(ProcessingStage.CLONE_DETECTED,versionedCounters,unversionedCounters),
                    getAggregatedValue(ProcessingStage.POV_INSTANCE_COMPILED,versionedCounters,unversionedCounters),
                    getAggregatedValue(ProcessingStage.POV_INSTANCE_SUREFIRE_REPORTS_GENERATED,versionedCounters,unversionedCounters),
                    getAggregatedValue(ProcessingStage.POV_INSTANCE_VULNERABILITY_CONFIRMED,versionedCounters,unversionedCounters),
                    getAggregatedValue(ProcessingStage.POV_INSTANCE_VULNERABILITY_CONFIRMED_SHADED,versionedCounters,unversionedCounters)
            ));
            latexOut.println("\t\\hline");

            latexOut.println("\t\\end{tabular}");
            latexOut.println("\t\\caption{\\label{tab:pipeline}Processed Artifacts at each stage, numbers in brackets are classes of artifacts with the same group and artifact id (i.e., versions are ignored)}");
            latexOut.println("\\end{table*}");
        }
    }

    private static void aggregate(Properties properties, ProcessingStage stage, Map<ProcessingStage,Integer> versionedCounters, Map<ProcessingStage,Integer> unversionedCounters) {
        String v = properties.getProperty(stage.name());
        if (v!=null) {
            int i = Integer.valueOf(v);
            final int j = i; // final for lambda
            versionedCounters.compute(stage,(k, oldValue) ->  oldValue==null ? j : j+oldValue);

            v = properties.getProperty(stage.name()+ ProgressReporter.UNVERSIONED_KEY_EXTENSION);
            if (v!=null) {
                i = Integer.valueOf(v);
                final int l = i; // final for lambda
                unversionedCounters.compute(stage,(k, oldValue) ->  oldValue==null ? l : l+oldValue);
            }
        }
    }

    // get the versioned / versioned values for a key (stage)
    private static int[] getValues(Properties properties, ProcessingStage stage) {
        String v = properties.getProperty(stage.name());
        if (v!=null) {
            int i = Integer.valueOf(v);
            v = properties.getProperty(stage.name()+ ProgressReporter.UNVERSIONED_KEY_EXTENSION);
            if (v!=null) {
                int j = Integer.valueOf(v);
                return new int[]{i,j};
            }
        }
        return null;
    }

    private static String getLatexValue(Properties properties, ProcessingStage stage) {
        int[] values = getValues(properties,stage);
        assert values.length==2;
        return String.format("%,d", values[0]) + " (" + String.format("%,d", values[1]) + ')';
    }

    private static String getCSVValue(Properties properties, ProcessingStage stage) {
        int[] values = getValues(properties,stage);
        assert values.length==2;
        // do not apply any formatting -- this creates issues when processing with R interpreting formatted ints as char
        // return String.format("%,d", values[0]) + CSV_SEPARATOR + String.format("%,d", values[1]);
        return values[0] + CSV_SEPARATOR + values[1];
    }

    private static String getAggregatedValue(ProcessingStage stage, Map<ProcessingStage,Integer> versionedCounters, Map<ProcessingStage,Integer> unversionedCounters) {
        return String.format("%,d", versionedCounters.get(stage))
            + " ("
            + String.format("%,d", unversionedCounters.get(stage))
            + ')';
    }

    private static String asLatexTableRow(Object... cellValues) {
        return Stream.of(cellValues)
            .map(obj -> obj.toString())
            .collect(Collectors.joining("&","\t"," \\\\"));
    }

    private static String asCSVRow(Object... cellValues) {
        return Stream.of(cellValues)
            .map(obj -> obj.toString())
            .collect(Collectors.joining(CSV_SEPARATOR));
    }

}
