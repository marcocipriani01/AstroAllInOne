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

    public String getUsbPort() {
        return usbPort;
    }

    public void setUsbPort(String usbPort) {
        this.usbPort = usbPort;
    }

    public String getIndiPort() {
        return indiPort;
    }

    public void setIndiPort(String indiPort) {
        this.indiPort = indiPort;
    }

    public String[] getDrivers() {
        return drivers;
    }

    public void setDrivers(String[] drivers) {
        this.drivers = drivers;
    }

    public int[] getDigitalPins() {
        return digitalPins;
    }

    public void setDigitalPins(int[] digitalPins) {
        this.digitalPins = digitalPins;
    }

    public int[] getPwmPins() {
        return pwmPins;
    }

    public void setPwmPins(int[] pwmPins) {
        this.pwmPins = pwmPins;
    }

    public int getShutterCablePin() {
        return shutterCablePin;
    }

    public void setShutterCablePin(int shutterCablePin) {
        this.shutterCablePin = shutterCablePin;
    }
}