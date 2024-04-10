package anonymized.shadedetector;

import anonymized.shadedetector.resultreporting.LogResultReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instantiates and sets up a ResultReporter from a configuration string.
 * @author jens dietrich
 */
public class ResultReporterFactory  extends  AbstractServiceLoaderFactory<ResultReporter> {
    @Override
    protected Logger getLogger() {
        return LoggerFactory.getLogger(ResultReporterFactory.class);
    }

    @Override
    public ResultReporter getDefault() {
        return new LogResultReporter();
    } //

    @Override
    public ResultReporter create(String configuration) {
        return create(ResultReporter.class,"result reporter",configuration);
    }
}
