package anonymized.shadedetector.cveverification;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utility to query and manipulate parsed Java code.
 * @author jens dietrich
 */

public class ASTUtils {

    private static Logger LOGGER = LoggerFactory.getLogger(ASTUtils.class);

    public static Predicate<ImportDeclaration> ALL = imp -> true;
    public static Predicate<ImportDeclaration> HAS_WILDCARDS = imp -> imp.isAsterisk();
    public static Predicate<ImportDeclaration> HAS_NO_WILDCARDS = imp -> !imp.isAsterisk();
    public static Predicate<ImportDeclaration> IS_STATIC = imp -> imp.isStatic();
    public static Predicate<ImportDeclaration> IS_NOT_STATIC = imp -> !imp.isStatic();

    public static List<String> getImports(Path src,Predicate<ImportDeclaration> ... filters) throws IOException {

        Predicate<ImportDeclaration> filter = ALL;
        for (Predicate<ImportDeclaration> f:filters) {
            filter = filter.and(f);
        }

        CompilationUnit cu = StaticJavaParser.parse(src);
        return cu.getImports().stream()
            .filter(filter)
            .map(imp -> imp.getName().asString())
            .collect(Collectors.toList());
    }

    public static String getFullyQualifiedClassname(Path src) throws IOException {
        CompilationUnit cu = StaticJavaParser.parse(src);
        return cu.getPrimaryType()
            .map(type -> type.getFullyQualifiedName())
            .get().orElseThrow(() -> new IllegalStateException("No class definition found in " + src));
    }

    /**
     * Updates imports.
     * @param projectFolderOrJavaSource
     * @param importTranslation
     * @return true if imports were changed, false otherwise
     * @throws IOException
     */
    public static boolean updateImports(Path projectFolderOrJavaSource, Map<String,String> importTranslation) throws IOException {

        boolean importsHaveChanged = false;
        if (Files.isDirectory(projectFolderOrJavaSource)) {
            List<Path> sources = Files.walk(projectFolderOrJavaSource)
                .filter(file -> !Files.isDirectory(file))
                .filter(file -> file.toFile().getName().endsWith(".java"))
                .collect(Collectors.toList());

            for (Path src : sources) {
                boolean importsHaveChanged2 = false;
                CompilationUnit cu = StaticJavaParser.parse(src);
                NodeList imports = cu.getImports();
                for (int i = 0; i < imports.size(); i++) {
                    ImportDeclaration imprt = (ImportDeclaration) imports.get(i);
                    String val = imprt.getNameAsString();
                    String newVal = importTranslation.get(val);
                    if (newVal != null && !val.equals(newVal)) {
                        imprt.setName(newVal);
                        importsHaveChanged2 = true;
                        importsHaveChanged = true;
                    }
                }

                if (importsHaveChanged2) {
                    LOGGER.info("writing java sources with updated imports");
                    Files.writeString(src, cu.toString());
                }
            }
        }
        else {
            CompilationUnit cu = StaticJavaParser.parse(projectFolderOrJavaSource);
            NodeList imports = cu.getImports();
            for (int i = 0; i < imports.size(); i++) {
                ImportDeclaration imprt = (ImportDeclaration) imports.get(i);
                String val = imprt.getNameAsString();
                String newVal = importTranslation.get(val);
                if (newVal != null && !val.equals(newVal)) {
                    imprt.setName(newVal);
                    importsHaveChanged = true;
                }
            }

            if (importsHaveChanged) {
                LOGGER.info("writing java sources with updated imports");
                Files.writeString(projectFolderOrJavaSource, cu.toString());
            }
        }
        return importsHaveChanged;
    }


}
