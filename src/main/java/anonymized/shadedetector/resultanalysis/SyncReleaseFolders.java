package anonymized.shadedetector.resultanalysis;

import com.google.common.base.Preconditions;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Synchronise two release folders.
 * @author jens dietrich
 */
public class SyncReleaseFolders {


    public static final boolean SIMULATE_REMOVAL = true ;
    public static final boolean SIMULATE_ADDITION = true ;

    /**
     *
     * @param args first arg -- actual repo, second arg -- temporary repo tp update actual repo
     * @throws Exception
     */
    public static void main (String[] args) throws Exception {

        Preconditions.checkState(args.length==2,"two arguments required -- the original repo, and the one to sync (add/remove)");

        File repo1 = new File(args[0]);
        Preconditions.checkState(repo1.exists());
        File repo2 = new File(args[1]);
        Preconditions.checkState(repo2.exists());

        List<String> CVEs = Stream.of(repo2.listFiles())
            .filter(f -> !f.isHidden())
            .filter(f -> f.isDirectory())
            .filter(f -> f.getName().startsWith("CVE-"))
            .map(f -> f.getName())
            .sorted()
            .collect(Collectors.toList());

        System.out.println("Consolidating results for the following CVEs: " + CVEs.stream().collect(Collectors.joining(",")));

        for (String cve:CVEs) {
            File cveDir1 = new File(repo1,cve);
            File cveDir2 = new File(repo2,cve);
            Set<File> projectsFolders1 = Stream.of(cveDir1.listFiles())
                .filter(f -> f.isDirectory())
                .filter(f -> !f.isHidden())
                .collect(Collectors.toSet());

            Set<File> projectsFolders2 = Stream.of(cveDir2.listFiles())
                    .filter(f -> f.isDirectory())
                    .filter(f -> !f.isHidden())
                    .collect(Collectors.toSet());

            // look for removals
            for (File projectFolder1:projectsFolders1) {
                File projectFolder2 = new File(cveDir2,projectFolder1.getName());
                if (!projectFolder2.exists()) {
                    if (SIMULATE_REMOVAL) {
                        System.out.println("simulate removal: " + projectFolder1.getAbsolutePath());
                    }
                    else {
                        System.out.println("removing: " + projectFolder1.getAbsolutePath());
                        FileUtils.deleteDirectory(projectFolder1);
                    }
                }
            }

            // look for additions
            for (File projectFolder2:projectsFolders2) {
                File projectFolder1 = new File(cveDir1,projectFolder2.getName());
                if (!projectFolder1.exists()) {
                    if (SIMULATE_ADDITION) {
                        System.out.println("simulate addition: " + projectFolder1.getAbsolutePath());
                    }
                    else {
                        System.out.println("adding: " + projectFolder1.getAbsolutePath());
                        FileUtils.copyDirectory(projectFolder2, projectFolder1);
                    }
                }
            }



        }


    }


}