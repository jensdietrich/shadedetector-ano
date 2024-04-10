package anonymized.shadedetector;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Given a list of files containing Java source code (i.e. *.java), return a list of those files
 * to be used to locate potential clones.
 * Then list return should contain unqualified class names (i.e. package names and .java extensions removed).
 * The order matters as this defines the order in which classes will be used in queries, and in some cases a subset might be used.
 * @author jens dietrich
 */
public interface ClassSelector extends NamedService {

    List<String> selectForSearch(Path folderOrZipContainingSources);

    default Set<String> getNamesAsSet (List<Path> sourceCodeList) {

        return sourceCodeList.stream()
            .map(f -> f.getFileName().toString())
            .filter(n -> n.endsWith(".java"))
            .map(n -> n.replace(".java", ""))
            .collect(Collectors.toSet());
    }

    default List<String> getNamesAsList (List<Path> sourceCodeList) {
        return sourceCodeList.stream()
            .map(f -> f.getFileName().toString())
            .filter(n -> n.endsWith(".java"))
            .map(n -> n.replace(".java", ""))
            .collect(Collectors.toList());
    }
}
