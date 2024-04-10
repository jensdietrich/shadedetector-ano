package anonymized.shadedetector.classselectors;

import anonymized.shadedetector.ClassSelector;
import anonymized.shadedetector.Utils;

import java.nio.file.Path;
import java.util.List;

/**
 * Select all classes (sources) from a given list.
 * @author jens dietrich
 */
public class SelectAll implements ClassSelector {

    @Override
    public String name() {
        return "all";
    }

    @Override
    public List<String> selectForSearch(Path folderOrZipContainingSources) {
        return  Utils.getUnqualifiedJavaClassNames(folderOrZipContainingSources);
    }
}
