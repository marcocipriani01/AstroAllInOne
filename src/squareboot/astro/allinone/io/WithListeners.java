package squareboot.astro.allinone.io;

/**
 * @param <C> the listeners' class.
 * @author SquareBoot
 * @version 0.1
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public interface WithListeners<C extends MessageListener> {

    /**
     * Adds a listener to the list.
     *
     * @param listener the new listener.
     */
    void addListener(C listener);
}