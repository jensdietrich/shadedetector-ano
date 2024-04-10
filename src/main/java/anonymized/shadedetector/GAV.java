package anonymized.shadedetector;

import com.google.common.base.Preconditions;

import java.util.Objects;

/**
 * Maven versioned artifact structure.
 * @author jens dietrich
 */
public class GAV {
    private String groupId = null;
    private String artifactId = null;
    private String version = null;

    public GAV(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public GAV(String id) {
        String[] tokens = id.split(":");
        Preconditions.checkArgument(tokens.length==3,"unexpected gav (3 tokens expected - groupId:artifactId:version): id");
        this.groupId = tokens[0];
        this.artifactId = tokens[1];
        this.version = tokens[2];
    }


    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GAV gav = (GAV) o;
        return Objects.equals(groupId, gav.groupId) && Objects.equals(artifactId, gav.artifactId) && Objects.equals(version, gav.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, artifactId, version);
    }

    public String asString() {
        return "" + groupId + ':' + artifactId + ':' + version ;
    }
}
