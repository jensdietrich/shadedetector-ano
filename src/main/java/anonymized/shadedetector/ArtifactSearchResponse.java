package anonymized.shadedetector;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class ArtifactSearchResponse {

    @SerializedName("responseHeader")
    private ResponseHeader header = null;
    @SerializedName("response")
    private ResponseBody body = null;

    public ResponseHeader getHeader() {
        return header;
    }

    public void setHeader(ResponseHeader header) {
        this.header = header;
    }

    public ResponseBody getBody() {
        return body;
    }

    public void setBody(ResponseBody body) {
        this.body = body;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArtifactSearchResponse that = (ArtifactSearchResponse) o;
        return Objects.equals(header, that.header) && Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(header, body);
    }
}
