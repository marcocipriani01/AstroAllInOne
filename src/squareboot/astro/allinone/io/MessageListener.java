package squareboot.astro.allinone.io;

/**
 * Listener interface for generic messages.
 *
 * @param <MessageType> the type of message.
 * @author SquareBoot
 * @version 0.1
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public interface MessageListener<MessageType> {

    /**
     * Invoked when a new message arrives.
     *
     * @param msg the received message.
     */
    void onMessage(MessageType msg);
}