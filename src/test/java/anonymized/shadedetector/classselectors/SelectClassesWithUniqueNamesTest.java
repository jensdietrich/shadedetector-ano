package anonymized.shadedetector.classselectors;

import anonymized.shadedetector.ArtifactSearchTest;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SelectClassesWithUniqueNamesTest {

    @Test
    public void test () throws IOException {
        URL url = ArtifactSearchTest.class.getClassLoader().getResource("commons-collections4-4.0");
        System.out.println("reading test data from url " + url);
        File sourceFolder = new File(url.getFile());
        System.out.println("reading test data from file " + sourceFolder.getAbsolutePath());


        List<String> sortedSources = new SelectClassesWithComplexNames().selectForSearch(sourceFolder.toPath());
        List<String> allSources = new SelectAll().selectForSearch(sourceFolder.toPath());

        // to test content
        Set<String> sourcesAsSet = new HashSet(allSources);
        Set<String> sortedSourcesAsSet = new HashSet<>(sortedSources);

        assertEquals(sortedSourcesAsSet,sortedSourcesAsSet);

        for (int i=1;i<sortedSources.size();i++) {
            String className1 = sortedSources.get(i-1);
            String className2 = sortedSources.get(i);
            assertTrue(
        SelectClassesWithComplexNames.tokenizeCamelCase(className1).length >=
                SelectClassesWithComplexNames.tokenizeCamelCase(className2).length);
        }
    }
}
