package anonymized.shadedetector.cveverification;

import com.google.common.base.Preconditions;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import javax.xml.XMLConstants;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Utilities to analyse surefire test results.
 * @author jens dietrich
 */
public class SurefireUtils {

    public static class TestResults  {
        private int testCount = 0;
        private int errorCount = 0;
        private int failureCount = 0;
        private int skippedCount = 0;

        public TestResults(int testCount, int errorCount, int failureCount, int skippedCount) {
            this.testCount = testCount;
            this.errorCount = errorCount;
            this.failureCount = failureCount;
            this.skippedCount = skippedCount;

            Preconditions.checkArgument(testCount >= (errorCount + failureCount + skippedCount));
        }

        public boolean hasTests() {
            return this.testCount > 0;
        }

        public boolean allTestsExecuted() {
            return this.skippedCount == 0;
        }

        public boolean allTestsSucceeded() {
            return this.errorCount == 0 && this.failureCount == 0;
        }

        public int getTestCount() {
            return testCount;
        }

        public int getErrorCount() {
            return errorCount;
        }

        public int getFailureCount() {
            return failureCount;
        }

        public int getSkippedCount() {
            return skippedCount;
        }

        public boolean assertExpectedOutcome(TestSignal signal) {
            Preconditions.checkState(getTestCount()>0);
            Preconditions.checkArgument(signal!=null);
            if (allTestsExecuted()) {
                if (signal== TestSignal.FAILURE) {
                    return getFailureCount()==getTestCount();
                }
                else if (signal== TestSignal.ERROR) {
                    return getErrorCount()==getTestCount();
                }
                else if (signal== TestSignal.SUCCESS) {
                    return getErrorCount()==0 && getFailureCount()==0;
                }
            }
            return false;
        }




        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestResults that = (TestResults) o;
            return testCount == that.testCount && errorCount == that.errorCount && failureCount == that.failureCount && skippedCount == that.skippedCount;
        }

        @Override
        public int hashCode() {
            return Objects.hash(testCount, errorCount, failureCount, skippedCount);
        }
    }

    static TestResults merge (Collection<TestResults> parts) {
        int testCount = parts.stream().mapToInt(p -> p.getTestCount()).sum();
        int errorCount = parts.stream().mapToInt(p -> p.getErrorCount()).sum();
        int failureCount = parts.stream().mapToInt(p -> p.getFailureCount()).sum();
        int skippedCount = parts.stream().mapToInt(p -> p.getSkippedCount()).sum();
        return new TestResults(testCount,errorCount,failureCount,skippedCount);
    }

    public static TestResults parseSurefireReports (Path folder) throws IOException, JDOMException {
        Preconditions.checkArgument(Files.exists(folder));
        Preconditions.checkArgument(Files.isDirectory(folder),"this function is for parsing a folder containing surefire reports in xml format");

        List<TestResults> results = Files.list(folder)
            .filter(p -> p.toString().endsWith(".xml"))
            .filter(Files::isRegularFile)
            .map(f -> {
                try {
                    return parseSurefireReport(f);
                }
                catch (Exception x) {
                    throw new RuntimeException(x);
                }
            })
            .collect(Collectors.toList());
        return merge(results);

    }

    public static TestResults parseSurefireReport (Path report) throws IOException, JDOMException {
        Preconditions.checkArgument(Files.exists(report));
        Preconditions.checkArgument(!Files.isDirectory(report),"this function is for parsing a single surefire report in XML format");
        //<testsuite xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="https://maven.apache.org/surefire/maven-surefire-plugin/xsd/surefire-test-report-3.0.xsd" version="3.0" name="ConfirmVulnerabilitiesTests" time="0.355" tests="2" errors="0" skipped="0" failures="0">
        Document doc = parseXML(report);
        Element root = doc.getRootElement();
        Preconditions.checkArgument(root.getName().equals("testsuite"),"unexpected root element in " + report + " - not a surefire report");
        //time="0.355" tests="2" errors="0" skipped="0" failures="0"

        int testCount = Integer.parseInt(root.getAttribute("tests").getValue());
        int errorCount = Integer.parseInt(root.getAttribute("errors").getValue());
        int failureCount = Integer.parseInt(root.getAttribute("failures").getValue());
        int skippedCount = Integer.parseInt(root.getAttribute("skipped").getValue());

        return new TestResults(testCount,errorCount,failureCount,skippedCount);
    }

    private static Document parseXML (Path xml) throws IOException, JDOMException {
        SAXBuilder sax = new SAXBuilder();
        sax.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        sax.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return sax.build(xml.toFile());
    }
}
