package anonymized.shadedetector.resultanalysis;

import com.google.common.base.Preconditions;
import anonymized.shadedetector.cveverification.ASTUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility to gather results from processing and generate a summary tex report.
 * Assumes that the project naming conventions are used where a project name is a GAV, with group / id / version
 * separated by "__".
 * Note that this will try to read a file masked-artifacts.txt that contains a list of artifacts (GAV regex)
 * to mask in the generated report.
 * @author jens dietrich
 */
public class ReportResultsByCVE {

    public static final String MASK_LIST = "masked-artifacts.txt";

    // CVEs we ignore for reporting in paper -- for instance, if they are redundant with other CVEs
    public static Set<String> IGNORED_CVEs = Set.of(
            "CVE-2015-7501"   // report almost identical CVE-2015-6420 instead !
    );

    static class Record {
        String cve = null;
        Map<String,Set<String>> artifactVersionMap = new TreeMap<>(); //#
        Map<String,Integer> artifactShadedVersionsCount = new HashMap<>(); //#

        @Override
        public String toString() {
            return "Record{" +
                "cve='" + cve + '\'' +
                '}';
        }
    }

    public static void main (String[] args) throws IOException {

        Preconditions.checkArgument(args.length==4,"three arguments required -- folder with povs, folder with cloned povs, report file, folder where artifact lists are exported to (one file by CVE)");

        File maskListDef = new File(MASK_LIST);
        Preconditions.checkState(maskListDef.exists(),"mask list definition not found (can use empty file): " + maskListDef);
        Set<Pattern> maskList = Files.readAllLines(maskListDef.toPath()).stream()
            .filter(line -> !line.trim().startsWith("#")) // remove comments
            .map(line -> Pattern.compile(line.trim()))
            .collect(Collectors.toSet());

        Predicate<String> shouldMask = gav -> maskList.stream().anyMatch(pattern -> pattern.matcher(gav).matches());
        AtomicInteger maskArtifactCounter = new AtomicInteger();


        File ORIGINAL = new File(args[0]);
        File FINAL = new File(args[1]);
        File OUT_VULNERABLE_ARTIFACTS = new File(args[2]);
        File CVE_OUT_FOLDER = new File(args[3]);

        Preconditions.checkArgument(FINAL.exists());
        Preconditions.checkArgument(ORIGINAL.exists());
        Preconditions.checkArgument(CVE_OUT_FOLDER.exists());


        List<String> cves = Stream.of(FINAL.listFiles())
            .filter(f -> f.getName().startsWith("CVE-"))
            .filter(f -> f.isDirectory())
            .filter(f -> !f.isHidden())
            .map(f -> f.getName())
            .filter(n -> !IGNORED_CVEs.contains(n))
            .sorted(String::compareTo)
            .collect(Collectors.toList());

        List<Record> records = new ArrayList<>();
        for (String cve:cves) {
            System.out.println("Analysing " + cve);

            Record record = new Record();
            record.cve = cve;

            File cveFinalFolder = new File(FINAL,cve);
            Preconditions.checkArgument(cveFinalFolder.exists());

            Set<String> tested = Stream.of(cveFinalFolder.listFiles())
                .filter(f -> f.isDirectory())
                .filter(f -> !f.isHidden())
                .map(f -> f.getName())
                .filter(n -> n.contains("__"))
                .collect(Collectors.toSet());

            File original = new File(ORIGINAL,cve);
            Preconditions.checkState(original.exists());

            Set<String> shaded = Stream.of(cveFinalFolder.listFiles())
                .filter(f -> f.isDirectory())
                .filter(f -> !f.isHidden())
                .filter(f -> f.getName().contains("__"))
                .filter(f -> haveImportsChanged(original.toPath(),f.toPath()))
                .map(f -> f.getName())
                .collect(Collectors.toSet());

            tested.stream().forEach(a ->
                {
                    String kernel = removeVersionFromProjectName(a);
                    kernel = kernel.replace("__",":");
                    String version = getVersionFromProjectName(a);
                    Set<String> versions = record.artifactVersionMap.computeIfAbsent(kernel,k -> new TreeSet<>());
                    versions.add(version);
                }
            );

            tested.stream().forEach(a ->
                {
                    String kernel = removeVersionFromProjectName(a);
                    kernel = kernel.replace("__",":");
                    int count = record.artifactShadedVersionsCount.computeIfAbsent(kernel,a2 -> 0);
                    if (shaded.contains(a)) {
                        count = count+1;
                    }
                    record.artifactShadedVersionsCount.put(kernel,count);
                }
            );

            System.out.println(record);
            records.add(record);

        }

        System.out.println("creating vulnerable artifacts report in " + OUT_VULNERABLE_ARTIFACTS.getAbsolutePath());

        try (PrintWriter out = new PrintWriter(new FileWriter(OUT_VULNERABLE_ARTIFACTS))) {
            out.println("\\begin{table*}");
            out.println("\t\\begin{tabular}{|lll|}");
            out.println("\t\\hline");
            out.println(asLatexTableRow(
                "groupId:versionId",
                "versions",
                "shaded"
            ));

            for  (Record record:records) {
                out.println("\t\\hline");
                out.println("\t\\multicolumn{3}{|c|}{" + record.cve+ "} \\\\");
                out.println("\t\\hline");
                for (String ga:record.artifactVersionMap.keySet()) {
                    out.println(asLatexTableRow(
                        latexize(shouldMask.test(ga)?"<masked-artifact-" + maskArtifactCounter.incrementAndGet() + ">":ga),
                        record.artifactVersionMap.get(ga).size(),
                        //  latexize(record.artifactVersionMap.get(ga).stream().collect(Collectors.joining(","))),
                        record.artifactShadedVersionsCount.get(ga) == 0 ? "no" :
                                (record.artifactShadedVersionsCount.get(ga) == record.artifactVersionMap.get(ga).size() ? "yes" : "(yes)")
                    ));
                }

            }
            out.println("\t\\hline");
            out.println("\t\\end{tabular}");
            out.println("\t\\caption{\\label{tab:vulnerableartifacts}Vulnerable Artifacts Detected}");
            out.println("\\end{table*}");
        }


        // print artifact ids by CVE
        for  (Record record:records) {
            File file = new File(CVE_OUT_FOLDER,record.cve + ".txt");
            List lines = new ArrayList();
            for (String artifact:record.artifactVersionMap.keySet()) {
                for (String version:record.artifactVersionMap.get(artifact)) {
                    lines.add(artifact + ":" + version);
                }
            }
            Files.write(file.toPath(),lines, StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("list of vulnerable artifacts written to " + file.getAbsolutePath());
        }

    }


    private static String asLatexTableRow(Object... cellValues) {
        return Stream.of(cellValues)
            .map(obj -> obj.toString())
            .collect(Collectors.joining("&","\t"," \\\\"));
    }

    static String removeVersionFromProjectName (String projectName) {
        String name = projectName.substring(0,projectName.lastIndexOf("__"));
        assert name.contains("__"); // between group and version
        assert name.indexOf("__") == name.lastIndexOf("__");
        return name;
    }

    static String latexize(String s) {
        return s.replace("_", "\\_")
            .replace(":", ":\\-")  // help hyphenation
            .replace(".",".\\-");
    }

    static String getVersionFromProjectName (String projectName) {
        return projectName.substring(projectName.lastIndexOf("__")+2);
    }

    static boolean haveImportsChanged(Path project1, Path project2)  {

        try {
            List<Path> sources = Files.walk(project1)
                .filter(file -> !Files.isDirectory(file))
                .filter(file -> file.toFile().getName().endsWith(".java"))
                .collect(Collectors.toList());

            for (Path src : sources) {
                Path src2 = project2.resolve(project1.relativize(src));
                assert Files.exists(src2);

                // System.out.println("comparing imports " + src + " and " + src2);
                Set<String> imports1 = ASTUtils.getImports(src).stream().collect(Collectors.toSet());
                Set<String> imports2 = ASTUtils.getImports(src2).stream().collect(Collectors.toSet());

                if (!imports1.equals(imports2)) {
                    return true;
                }
            }
        }
        catch (IOException x) {
            x.printStackTrace();
        }

        return false;
    }
}
