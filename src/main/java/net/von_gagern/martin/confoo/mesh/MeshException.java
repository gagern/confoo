package net.von_gagern.martin.confoo.mesh;

/**
 * Exception thrown for unsuitable mesh configurations.
 *
 * @author <a href="mailto:Martin.vGagern@gmx.net">Martin von Gagern</a>
 * @since 1.0
 */
public class MeshException extends Exception {

    /**
     * Construct with given message.
     * @param message a message describing the exception
     */
    public MeshException(String message) {
        super(message);
    }

    /**
     * Construct with given message and wrapping another throwable.
     * @param message a message describing the exception
     * @param cause the cause of the exception
     */
    public MeshException(String message, Throwable cause) {
        super(message, cause);
    }

}
