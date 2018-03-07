package squareboot.astro.allinone;

import javax.swing.*;

/**
 * @author SquareBoot
 * @version 0.1
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class PwmPinPanel extends AbstractPinPanel {

    /**
     * The panel.
     */
    private JPanel parent;
    /**
     * The name.
     */
    private JTextField nameTextField;
    /**
     * The value.
     */
    private JSpinner valueSpinner;
    /**
     * The label with the pin ID.
     */
    private JLabel pinLabel;

    /**
     * Class constructor.
     *
     * @param pin a pin.
     */
    public PwmPinPanel(ArduinoPin pin) {
        super(pin);
        pinLabel.setText("Pin " + pin.getPin() + ":");
        nameTextField.setText(pin.getName());
        valueSpinner.setValue(pin.getValue());
    }

    /**
     * @return the {@link JPanel} represented by this object.
     */
    @Override
    public JPanel getPanel() {
        return parent;
    }
}