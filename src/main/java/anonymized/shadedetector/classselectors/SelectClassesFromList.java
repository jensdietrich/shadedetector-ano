package anonymized.shadedetector.classselectors;

import anonymized.shadedetector.ClassSelector;
import anonymized.shadedetector.Utils;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static anonymized.shadedetector.Utils.loadClassListFromFile;

/**
 * Explicitly specify classes (names) to look for.
 * The main use case is to identify classes necessary to exploit a given vulnerability.
 * Often, this can be found be a simple analysis of github projects created to demonstrate
 * vulnerabilities, such as https://github.com/frohoff/ysoserial.
 * There is a verification step that checks that classes with such names actually exist.
 * If not, an IllegalStateException will be thrown.
 * @author jens dietrich
 */
public class SelectClassesFromList implements ClassSelector {

    private static Logger LOGGER = LoggerFactory.getLogger(SelectClassesFromList.class);

    private List<String> classList = null;

    public SelectClassesFromList(List<String> classList) {
        this.classList = classList;
    }

    public SelectClassesFromList() {}

    @Override
    public String name() {
        return "list";
    }

    @Override
    public List<String> selectForSearch(Path folderOrZipContainingSources) {
        Set<String> classNames = new HashSet<>(Utils.getUnqualifiedJavaClassNames(folderOrZipContainingSources));
        for (String name:classList) {
            Preconditions.checkState(classNames.contains(name),"No class found in sourceCodeList named \"" + name + "\"");
        }
        return classList;
    }


    /**
     * Set a file name containing a list, to be used in bean instantiation.
     * @param fileName
     */
    public void setFile(String fileName) {
        File file = new File(fileName);
        Preconditions.checkArgument(file.exists());
        try {
            this.classList = Utils.loadClassListFromFile(file);
        } catch (IOException e) {
            LOGGER.error("Error loading classlist from " + fileName,e);
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Set a class names using a list encoded as a comma-separated string.
     * @param list
     */
    public void setList(String list) {
        this.classList = Stream.of(list.split(",")).collect(Collectors.toList());
    }

    public List<String> getClassList() {
        return classList;
    }
}
