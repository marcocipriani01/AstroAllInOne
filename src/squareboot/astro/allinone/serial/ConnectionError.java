package squareboot.astro.allinone.serial;

/**
 * Exception related to connections, sockets, communication and data trasfer in general.
 *
 * @author SquareBoot
 * @version 0.1
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ConnectionError extends RuntimeException {

    /**
     * The kind of the error occurred.
     */
    private Type type;

    /**
     * Constructs a new exception with {@code null} as its detail message.
     */
    public ConnectionError() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public ConnectionError(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and
     * cause.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method).  (A <tt>null</tt> value is
     *                permitted, and indicates that the cause is nonexistent or
     *                unknown.)
     */
    public ConnectionError(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with {@code null} as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     *
     * @param type the type of error detected.
     * @see Type
     */
    public ConnectionError(Type type) {
        super();
        this.type = type;
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     * @param type    the type of error detected.
     * @see Type
     */
    public ConnectionError(String message, Type type) {
        super(message);
        this.type = type;
    }

    /**
     * Constructs a new exception with the specified detail message and
     * cause.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method).  (A <tt>null</tt> value is
     *                permitted, and indicates that the cause is nonexistent or
     *                unknown.
     * @param type    the type of error detected.
     * @see Type
     */
    public ConnectionError(String message, Throwable cause, Type type) {
        super(message, cause);
        this.type = type;
    }

    /**
     * Constructs a new exception with the specified cause and a detail
     * message of <tt>(cause==null ? null : cause.toString())</tt> (which
     * typically contains the class and detail message of <tt>cause</tt>).
     *
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link #getCause()} method).  (A <tt>null</tt> value is
     *              permitted, and indicates that the cause is nonexistent or
     *              unknown.)
     * @param type  the type of error detected.
     * @see Type
     */
    public ConnectionError(Throwable cause, Type type) {
        super(cause);
        this.type = type;
    }

    /**
     * @return the type of error detected.
     * @see Type
     */
    public Type getType() {
        return type;
    }

    /**
     * Enum representing the kind of error detected.
     *
     * @author SquareBoot
     * @version 0.1
     */
    public enum Type {
        /**
         * Generic or unknown error.
         */
        UNKNOWN,
        /**
         * Not connected.
         */
        NOT_CONNECTED,
        /**
         * Not started yet.
         */
        NOT_STARTED,
        /**
         * Already started.
         */
        ALREADY_STARTED,
        /**
         * Already connected.
         */
        ALREADY_CONNECTED,
        /**
         * Generic, port busy.
         */
        BUSY,
        /**
         * Error during the I/O.
         */
        IO,
        /**
         * Error in input transfer.
         */
        INPUT,
        /**
         * Error in output transfer.
         */
        OUTPUT,
        /**
         * Error during connection.
         */
        CONNECTION,
        /**
         * Error during connection, port busy.
         */
        PORT_BUSY,
        /**
         * Error during connection, no port found.
         */
        PORT_NOT_FOUND,
        /**
         * Error during disconnection.
         */
        UNABLE_TO_DISCONNECT,
        /**
         * Error that occurs when the client doesn't receive a response to an important request.
         */
        NO_RESPONSE,
        /**
         * Error that occurs when the client doesn't receive a valid response to a request, or a received message was invalid.
         * Could be both a warning or a fatal error.
         */
        PROTOCOL,
        /**
         * Occurs when the network interfaces are unreachable.
         */
        NETWORK_ERROR
    }
}