package squareboot.astro.allinone;

import javax.swing.*;
import java.awt.*;

/**
 * @author SquareBoot
 * @version 0.1
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class AbstractPinDialog extends JDialog {

    /**
     * A pin.
     */
    private ArduinoPin pin;

    /**
     * Class constructor.
     *
     * @param pin a pin.
     */
    public AbstractPinDialog(JFrame frame, ArduinoPin pin) {
        super(frame, "Pin editor", Dialog.ModalityType.DOCUMENT_MODAL);
        setIconImage(ControlPanel.APP_LOGO);
        this.pin = pin;
    }

    /**
     * @return the stored pin.
     */
    public ArduinoPin getArduinoPin() {
        return pin;
    }
}