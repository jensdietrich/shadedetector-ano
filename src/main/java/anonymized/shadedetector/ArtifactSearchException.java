package anonymized.shadedetector;

public class ArtifactSearchException extends Exception {


    public ArtifactSearchException() {
    }

    public ArtifactSearchException(String message) {
        super(message);
    }

    public ArtifactSearchException(String message, Throwable cause) {
        super(message, cause);
    }

    public ArtifactSearchException(Throwable cause) {
        super(cause);
    }

    public ArtifactSearchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
