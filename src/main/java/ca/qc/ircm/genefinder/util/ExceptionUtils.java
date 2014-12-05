package ca.qc.ircm.genefinder.util;

/**
 * Utilities for exceptions.
 */
public class ExceptionUtils {
    /**
     * Returns exception if it's already a {@link RuntimeException}. If exception is not a {@link RuntimeException},
     * exception is packaged into a new {@link PackagedRuntimeException}.
     * 
     * @param e
     *            exception to package, if necessary
     * @param message
     *            message to put if a {@link PackagedRuntimeException} is created
     * @return exception if it's already a {@link RuntimeException} or a new {@link PackagedRuntimeException} with e as
     *         cause
     */
    public static RuntimeException optionallyPackageRuntimeException(Throwable e, String message) {
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        } else {
            return new PackagedRuntimeException(message, e);
        }
    }

    /**
     * Throws exception if it's assignable to clazz. Otherwise, this method does nothing.
     * 
     * @param exception
     *            exception
     * @param clazz
     *            expected exception's clazz
     */
    public static <T extends Throwable> void throwExceptionIfMatch(Throwable exception, Class<? extends T> clazz)
            throws T {
        if (exception != null && clazz.isAssignableFrom(exception.getClass())) {
            @SuppressWarnings("unchecked")
            T exceptionCast = (T) exception;
            throw exceptionCast;
        }
    }

    /**
     * Throws an {@link InterruptedException} if current thread is interrupted.
     * 
     * @param message
     *            exception's message
     */
    public static void throwIfInterrupted(String message) throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException(message);
        }
    }

    /**
     * Returns first found expection's cause in expection's causes that matches expected class.
     * 
     * @param e
     *            exception
     * @param clazz
     *            expected class
     * @return expection's cause if it matches expected class
     */
    public static <T extends Throwable> T getCause(Throwable e, Class<T> clazz) {
        if (e != null) {
            Throwable cause = e.getCause();
            while (cause != null) {
                if (clazz.isAssignableFrom(cause.getClass())) {
                    @SuppressWarnings("unchecked")
                    T ret = (T) cause;
                    return ret;
                } else {
                    cause = cause.getCause();
                }
            }
        }
        return null;
    }
}
