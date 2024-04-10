package anonymized.shadedetector;

import anonymized.shadedetector.classselectors.SelectClassesWithComplexNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instantiates and sets up a ClassSelector from a configuration string.
 * @author jens dietrich
 */
public class ClassSelectorFactory extends  AbstractServiceLoaderFactory<ClassSelector> {

    @Override
    protected Logger getLogger() {
        return LoggerFactory.getLogger(ClassSelectorFactory.class);
    }

    @Override
    public ClassSelector getDefault() {
        return new SelectClassesWithComplexNames();
    }

    @Override
    public ClassSelector create(String configuration) {
        return create(ClassSelector.class,"class selector",configuration);
    }
}
