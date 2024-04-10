package anonymized.shadedetector.cveverification;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility to run mvn.
 * @author jens dietrich
 */
public class MVNExe {

    public static ProcessResult mvnCleanTest(Path projectFolder, Map<String,String> environmentVariables, String... phases) throws Exception {
        return mvn(projectFolder,environmentVariables,"clean","test");
    }

    public static ProcessResult mvnCleanTest(Path projectFolder, String... phases) throws Exception {
        return mvn(projectFolder, Collections.EMPTY_MAP,"clean","test");
    }

    public static ProcessResult mvnTest(Path projectFolder, Properties environment, String... phases) throws Exception {
        return mvn(projectFolder, Maps.newHashMap(Maps.fromProperties(environment)),"test");
    }

    public static ProcessResult mvnTest(Path projectFolder, Map<String,String> environment, String... phases) throws Exception {
        return mvn(projectFolder, environment,"test");
    }

    public static ProcessResult mvnCleanCompile(Path projectFolder, Map<String,String> environmentVariables, String... phases) throws Exception {
        return mvn(projectFolder,environmentVariables,"clean","compile");
    }

    public static ProcessResult mvnCleanCompile(Path projectFolder) throws Exception {
        return mvn(projectFolder, Collections.EMPTY_MAP,"clean","compile");
    }

    public static ProcessResult mvnCleanCompile(Path projectFolder,Properties environment) throws Exception {
        // Based on its implementation (https://guava.dev/releases/23.0/api/docs/src-html/com/google/common/collect/Maps.html#line.1399),
        // Maps.fromProperties() calls Properties::propertyNames() and thus *will* include non-overridden default properties.
        return mvn(projectFolder, Maps.newHashMap(Maps.fromProperties(environment)),"clean","compile");
    }

    public static ProcessResult mvnCleanCompile(Path projectFolder,Map<String,String> environment) throws Exception {
        return mvn(projectFolder, environment,"clean","compile");
    }



    public static ProcessResult mvn(Path projectFolder, Map<String,String> environmentVariables, String... phases) throws Exception {

        Preconditions.checkArgument(Files.exists(projectFolder));
        Preconditions.checkArgument(Files.isDirectory(projectFolder));
        Path pom = projectFolder.resolve("pom.xml");
        Preconditions.checkArgument(Files.exists(pom));

        String[] cmd = new String[phases.length+1 ];
        int index = 1;


        cmd[0] = System.getProperty("os.name").toLowerCase().contains("win") ? "mvn.cmd" : "mvn";
        for (int i=0;i<phases.length;i++) {
            cmd[index] = phases[i];
            index = index+1;
        }

        ProcessResult result = new ProcessExecutor()
            .readOutput(true)
            .directory(projectFolder.toFile())
            .command(cmd)
            .environment(environmentVariables)
            .execute();

        // List<String> output = extractOutput(result);

        return result;
    }


    public static List<String> extractOutput(ProcessResult result) {
        List<String> lines = new ArrayList<>();
        if (result.getExitValue()!=0) {
            String output = result.outputString();
            if (output!=null) {
                lines = new BufferedReader(new StringReader(output))
                    .lines()
                    .collect(Collectors.toList());
            }
        }
        return lines;
    }
}
