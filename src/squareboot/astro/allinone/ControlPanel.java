package squareboot.astro.allinone;

import squareboot.astro.allinone.io.Arduino;

import javax.swing.*;
import java.util.ArrayList;

/**
 * @author SquareBoot
 * @version 0.1
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ControlPanel extends JFrame {

    /**
     * The parent component.
     */
    private JPanel parent;
    private JPanel digitalPinsPanel;
    private JPanel pwmPinsPanel;
    private JSpinner shutterCablePin;
    private JButton okButton;
    private JButton cancelButton;
    private JComboBox<String> portsComboBox;
    private JSpinner indiPortField;
    private JList driversList;
    private JButton button1;

    /**
     * Class constructor.
     */
    public ControlPanel() {
        super("AstroAllInOne control panel");
        setContentPane(parent);
        ArrayList<String> ports = Arduino.listAvailablePorts();
        for (String p : ports) {
            portsComboBox.addItem(p);
        }

        pack();
    }

    private void createUIComponents() {
        indiPortField = new JSpinner(new SpinnerNumberModel(7624, 10, 99999, 1));
        digitalPinsPanel = new JPanel();
        pwmPinsPanel = new JPanel();
    }
}