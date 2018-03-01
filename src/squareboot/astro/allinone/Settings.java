package squareboot.astro.allinone;

/**
 * Stores all the app's settings.
 *
 * @author SquareBoot
 * @version 0.1
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Settings {

    protected String usbPort;
    protected String indiPort;
    protected String[] drivers;
    protected int[] digitalPins;
    protected int[] pwmPins;
    protected int shutterCablePin;

    /**
     * Class constructor.
     */
    public Settings() {

    }
}