package anonymized.shadedetector;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Objects;

/**
 * Representation of a Maven artifact.
 * Annotations are used to map this to JSON representation returned by the Maven REST API.
 * @author jens dietrich
 */
public class Artifact {
    private String id = null;

    @SerializedName("g")
    private String groupId = null;

    @SerializedName("a")
    private String artifactId = null;

    @SerializedName("v")
    private String version = null;

    @SerializedName("p")
    private String projectType = null;

    private long timestamp = 0;

    @SerializedName("ec")
    private List<String> resources = null;

    private List<String> tags = null;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getResources() {
        return resources;
    }

    public void setResources(List<String> resources) {
        this.resources = resources;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public GAV asGAV() {
        return new GAV(this.groupId,this.artifactId,this.version);
    }

    @Override
    public String toString() {
        return asGAV().asString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Artifact artifact = (Artifact) o;
        return timestamp == artifact.timestamp && Objects.equals(id, artifact.id) && Objects.equals(groupId, artifact.groupId) && Objects.equals(artifactId, artifact.artifactId) && Objects.equals(version, artifact.version) && Objects.equals(projectType, artifact.projectType) && Objects.equals(resources, artifact.resources) && Objects.equals(tags, artifact.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, groupId, artifactId, version, projectType, timestamp, resources, tags);
    }
}

