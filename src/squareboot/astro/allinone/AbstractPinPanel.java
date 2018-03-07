package squareboot.astro.allinone;

import javax.swing.*;

/**
 * @author SquareBoot
 * @version 0.1
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class AbstractPinPanel {

    /**
     * A pin.
     */
    private ArduinoPin pin;

    /**
     * Class constructor.
     *
     * @param pin a pin.
     */
    public AbstractPinPanel(ArduinoPin pin) {
        this.pin = pin;
    }

    /**
     * @return the stored pin.
     */
    public ArduinoPin getArduinoPin() {
        return pin;
    }

    /**
     * @return the {@link JPanel} represented by this object.
     */
    public abstract JPanel getPanel();
}