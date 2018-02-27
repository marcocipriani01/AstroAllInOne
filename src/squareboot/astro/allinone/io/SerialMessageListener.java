package squareboot.astro.allinone.io;

/**
 * Arduino serial message listener interface.
 *
 * @author SquareBoot
 * @version 0.1
 * @see Arduino
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public interface SerialMessageListener extends MessageListener<String> {

    /**
     * Called when a new message is received from the Arduino.
     *
     * @param msg the received message.
     */
    @Override
    void onMessage(final String msg);

    /**
     * Called when an error occurs.
     *
     * @param e the {@code Exception}.
     */
    void onConnectionError(Exception e);
}