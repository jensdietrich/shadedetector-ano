package anonymized.shadedetector;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Objects;

public class ResponseBody {
    private int numFound = 0;
    private int start = 0;

    @SerializedName("docs")
    private List<Artifact> artifacts = null;

    public List<Artifact> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(List<Artifact> artifacts) {
        this.artifacts = artifacts;
    }

    public int getNumFound() {
        return numFound;
    }

    public void setNumFound(int numFound) {
        this.numFound = numFound;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResponseBody that = (ResponseBody) o;
        return numFound == that.numFound && start == that.start && Objects.equals(artifacts, that.artifacts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numFound, start, artifacts);
    }
}
