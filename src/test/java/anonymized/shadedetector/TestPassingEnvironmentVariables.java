package anonymized.shadedetector;

import org.junit.jupiter.api.Test;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

import java.io.File;
import java.util.Map;

public class TestPassingEnvironmentVariables {

    @Test
    public void test () throws Exception {
        //Map<String,String> environment = Map.of("JAVA_HOME","/Library/Java/JavaVirtualMachines/jdk1.8.0_311.jdk/Contents/Home");
        Map<String,String> environment = Map.of("JAVA_HOME","/Library/Java/JavaVirtualMachines/jdk-11.0.11.jdk/Contents/Home");

        ProcessResult result = new ProcessExecutor()
            .readOutput(true)
            .directory(new File("."))
            .command("java","-version")
            .environment(environment)
            .execute();

        System.out.println("JVM running this program:");
        System.out.println(System.getProperty("java.version"));
        System.out.println();
        String out = result.outputString();
        System.out.println("JVM of external process:");
        System.out.println(out);
    }
}
