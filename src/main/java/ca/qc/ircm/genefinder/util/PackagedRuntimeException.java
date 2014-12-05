package ca.qc.ircm.genefinder.util;

/**
 * Used to package a non-runtime exception into a runtime exception.
 */
public class PackagedRuntimeException extends RuntimeException {
    private static final long serialVersionUID = -5792531631900410211L;

    public PackagedRuntimeException(Throwable cause) {
        super(cause);
    }

    public PackagedRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}