package anonymized.shadedetector.resultanalysis;

import java.nio.file.Path;
import java.util.Set;

/**
 * Abstraction to analyse results from tools run on a project, storing the results in some file (json, xml, ..) .
 * See src/main/resources for SCA scripts.
 * @author jens dietrich
 */
public interface SCAResultAnalysis {
    Set<String> getDetectedCVEs(Path resultFileOrFolder) ;
}
