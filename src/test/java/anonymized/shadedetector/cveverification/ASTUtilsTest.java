package anonymized.shadedetector.cveverification;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ASTUtilsTest {

    private static final String ORIGINAL_SRC = "/sources/Driver.java";
    private static final String MANIPULATED_SRC = "/sources/Driver.java.clone";
    private Path originalSRC = null;
    private Path manipulatedSRC = null;


    @BeforeEach
    public void setup() throws IOException {
        originalSRC = Path.of(ASTUtilsTest.class.getResource(ORIGINAL_SRC).getPath());
        manipulatedSRC = Path.of(originalSRC.toString().replace(ORIGINAL_SRC,MANIPULATED_SRC));
        Assumptions.assumeTrue(Files.exists(originalSRC));
        Files.copy(originalSRC,manipulatedSRC, StandardCopyOption.REPLACE_EXISTING);
        Assumptions.assumeTrue(Files.exists(manipulatedSRC));
    }

    @AfterEach
    public void tearDown() throws IOException {
        if (Files.exists(manipulatedSRC)) {
            Files.delete(manipulatedSRC);
        }
    }

    @Test
    public void testAllImports() throws IOException {
        List<String> imports = ASTUtils.getImports(originalSRC);
        assertEquals(2,imports.size());
        assertTrue(imports.contains("org.yaml.snakeyaml.Yaml"));
        assertTrue(imports.contains("java.io"));
    }

    @Test
    public void testAllNonStaticImports() throws IOException {
        List<String> imports = ASTUtils.getImports(originalSRC,ASTUtils.IS_NOT_STATIC);
        assertEquals(2,imports.size());
        assertTrue(imports.contains("org.yaml.snakeyaml.Yaml"));
        assertTrue(imports.contains("java.io"));
    }

    @Test
    public void testAllNonWildcardImports() throws IOException {
        List<String> imports = ASTUtils.getImports(originalSRC,ASTUtils.HAS_NO_WILDCARDS);
        assertEquals(1,imports.size());
        assertTrue(imports.contains("org.yaml.snakeyaml.Yaml"));
    }

    @Test
    public void testAllWildcardImports() throws IOException {
        List<String> imports = ASTUtils.getImports(originalSRC,ASTUtils.HAS_WILDCARDS);
        assertEquals(1,imports.size());
        assertTrue(imports.contains("java.io"));
    }

    @Test
    public void testAllNonWildcardNonStaticImports() throws IOException {
        List<String> imports = ASTUtils.getImports(originalSRC,ASTUtils.HAS_NO_WILDCARDS,ASTUtils.IS_NOT_STATIC);
        assertEquals(1,imports.size());
        assertTrue(imports.contains("org.yaml.snakeyaml.Yaml"));
    }

    @Test
    public void testUpdateImports() throws IOException {
        List<String> imports = ASTUtils.getImports(manipulatedSRC,ASTUtils.HAS_NO_WILDCARDS,ASTUtils.IS_NOT_STATIC);
        Assumptions.assumeTrue(imports.size()==1);
        Assumptions.assumeTrue(imports.contains("org.yaml.snakeyaml.Yaml"));

        Map<String,String> mapping = Map.of("org.yaml.snakeyaml.Yaml","com.example.Yaml");

        ASTUtils.updateImports(manipulatedSRC,mapping);

        List<String> imports2 = ASTUtils.getImports(manipulatedSRC,ASTUtils.HAS_NO_WILDCARDS,ASTUtils.IS_NOT_STATIC);
        Assumptions.assumeTrue(imports2.size()==1);
        Assumptions.assumeTrue(imports2.contains("com.example.Yaml"));

    }

    @Test
    public void testExtractingFullyQualifiedClassName() throws IOException {
        String qName = ASTUtils.getFullyQualifiedClassname(originalSRC);
        assertEquals("com.example.Driver",qName);
    }

}
