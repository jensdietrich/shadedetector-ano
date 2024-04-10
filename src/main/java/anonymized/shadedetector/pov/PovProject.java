package anonymized.shadedetector.pov;

import anonymized.shadedetector.cveverification.TestSignal;

import java.util.List;
import java.util.Objects;

/**
 * Representation of an instance of the pov-project schema (see https://github.com/jensdietrich/xshady).
 * @author jens dietrich
 */
public class PovProject {
    private String id = null;
    private String artifact = null;
    private String fixVersion = null;
    private TestSignal testSignalWhenVulnerable = null;
    private List<String> vulnerableVersions = null;

    private String jdkVersion = null;
    private List<String> references = null;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getArtifact() {
        return artifact;
    }

    public void setArtifact(String artifact) {
        this.artifact = artifact;
    }

    public String getFixVersion() {
        return fixVersion;
    }

    public void setFixVersion(String fixVersion) {
        this.fixVersion = fixVersion;
    }

    public TestSignal getTestSignalWhenVulnerable() {
        return testSignalWhenVulnerable;
    }

    public void setTestSignalWhenVulnerable(TestSignal testSignalWhenVulnerable) {
        this.testSignalWhenVulnerable = testSignalWhenVulnerable;
    }

    public List<String> getVulnerableVersions() {
        return vulnerableVersions;
    }

    public void setVulnerableVersions(List<String> vulnerableVersions) {
        this.vulnerableVersions = vulnerableVersions;
    }

    public String getJdkVersion() { return jdkVersion; }

    public void setJdkVersion(String jdkVersion) { this.jdkVersion = jdkVersion;}

    public List<String> getReferences() {
        return references;
    }

    public void setReferences(List<String> references) {
        this.references = references;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PovProject that = (PovProject) o;
        return Objects.equals(id, that.id) && Objects.equals(artifact, that.artifact) && Objects.equals(fixVersion, that.fixVersion) && testSignalWhenVulnerable == that.testSignalWhenVulnerable && Objects.equals(vulnerableVersions, that.vulnerableVersions) && Objects.equals(references, that.references);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, artifact, fixVersion, testSignalWhenVulnerable, vulnerableVersions, references);
    }
}
