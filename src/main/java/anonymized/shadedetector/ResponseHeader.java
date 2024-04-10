package anonymized.shadedetector;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class ResponseHeader {

    static class Parameters {
        private String sort = null;
        private int rows = 0;
        private String version = null;

        public String getSort() {
            return sort;
        }

        public void setSort(String sort) {
            this.sort = sort;
        }

        public int getRows() {
            return rows;
        }

        public void setRows(int rows) {
            this.rows = rows;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Parameters that = (Parameters) o;
            return rows == that.rows && Objects.equals(sort, that.sort) && Objects.equals(version, that.version);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sort, rows, version);
        }
    }
    private int status = 0;
    @SerializedName("QTime")
    private int qtime = 0;
    @SerializedName("params")
    private Parameters parameters = null;

    public Parameters getParameters() {
        return parameters;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getQtime() {
        return qtime;
    }

    public void setQtime(int QTime) {
        this.qtime = QTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResponseHeader that = (ResponseHeader) o;
        return status == that.status && qtime == that.qtime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, qtime);
    }
}
