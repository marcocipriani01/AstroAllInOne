package squareboot.astro.allinone;

import squareboot.astro.allinone.io.Arduino;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The control panel.
 *
 * @author SquareBoot
 * @version 0.1
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ControlPanel extends JFrame implements ActionListener {

    /**
     * The parent component.
     */
    private JPanel parent;
    private JPanel digitalPinsPanel;
    private JPanel pwmPinsPanel;
    private JButton okButton;
    private JButton cancelButton;
    private JComboBox<String> portsComboBox;
    private JSpinner indiPortField;
    private JButton refreshButton;
    private JButton addDigitalPinButton;
    private JButton removeDigitalPinButton;
    private JButton addPwmPinButton;
    private JButton removePwmPinButton;
    private ArrayList<String> serialPorts;
    private Timer refresher = new Timer("Serial ports refresher");
    private Settings settings;

    /**
     * Class constructor.
     */
    public ControlPanel() {
        super("AstroAllInOne control panel");
        setIconImage(Main.logo);
        setContentPane(parent);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        settings = Main.getSettings();

        serialPorts = Arduino.listAvailablePorts();
        for (String p : serialPorts) {
            portsComboBox.addItem(p);
        }
        refreshButton.addActionListener(e -> refreshPorts());
        refresher.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                refreshPorts();
            }
        }, 1000, 1000);

        cancelButton.addActionListener(e -> dispose());
        okButton.addActionListener(e -> {
            settings.usbPort = (String) portsComboBox.getSelectedItem();
            settings.indiPort = (int) indiPortField.getValue();
            //Main.settings.save(Main.file); //TODO
            dispose();
        });

        portsComboBox.setSelectedItem(settings.usbPort);
        indiPortField.setValue(settings.indiPort);

        setBounds(200, 150, 650, 550);
        setVisible(true);
    }

    @Override
    public void dispose() {
        super.dispose();
        refresher.cancel();
    }

    /**
     * Refreshes all the serial ports.
     */
    private void refreshPorts() {
        ArrayList<String> newPorts = Arduino.listAvailablePorts();
        if (!newPorts.equals(serialPorts)) {
            portsComboBox.removeAllItems();
            for (String p : serialPorts) {
                portsComboBox.addItem(p);
            }
        }
    }

    private void createUIComponents() {
        indiPortField = new JSpinner(new SpinnerNumberModel(7624, 10, 99999, 1));
        digitalPinsPanel = new JPanel();
        pwmPinsPanel = new JPanel();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == addDigitalPinButton) {
            ArduinoPin pin = askNewPin();
            if (pin != null) {
                settings.digitalPins.add(pin);
                // TODO add to panel
            }

        } else if (source == removeDigitalPinButton) {


        } else if (source == addPwmPinButton) {


        } else if (source == removePwmPinButton) {

        }
    }

    private ArduinoPin askNewPin() {
        try {
            int parsed = Integer.valueOf(JOptionPane.showInputDialog(this, "New pin",
                    "Control panel", JOptionPane.QUESTION_MESSAGE));
            ArduinoPin pin = new ArduinoPin();
            pin.setPin(parsed);
            return pin;

        } catch (NullPointerException e) {
            return null;
        }
    }
}