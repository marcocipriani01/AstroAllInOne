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
public abstract class ControlPanel extends JFrame implements ActionListener {

    /**
     * The parent component.
     */
    private JPanel parent;
    /**
     * The list of digital pins.
     */
    private JList<AbstractPinPanel> digitalPinsList;
    /**
     * The list of PWM pins.
     */
    private JList<AbstractPinPanel> pwmPinsList;
    /**
     * Save button.
     */
    private JButton okButton;
    /**
     * Cancel button.
     */
    private JButton cancelButton;
    /**
     * ComboBox containing the list of available serial ports.
     */
    private JComboBox<String> portsComboBox;
    /**
     * Field for the INDI server's port.
     */
    private JSpinner indiPortField;
    /**
     * Button to refresh the list of serial ports
     */
    private JButton refreshButton;
    /**
     * Button to add a new digital pin.
     */
    private JButton addDigitalPinButton;
    /**
     * Button to remove a digital pin.
     */
    private JButton removeDigitalPinButton;
    /**
     * Button to add a new PWM pin.
     */
    private JButton addPwmPinButton;
    /**
     * Button to remove a PWM pin.
     */
    private JButton removePwmPinButton;
    /**
     * List of available serial ports.
     */
    private ArrayList<String> serialPorts;
    /**
     * Timer to refresh the list of available serial ports.
     */
    private Timer refresher = new Timer("Serial ports refresher");
    /**
     * @see Main#getSettings()
     */
    private Settings settings;
    /**
     * Model for the list of digital pins.
     */
    private DefaultListModel<AbstractPinPanel> digitalPinsModel = new DefaultListModel<>();
    /**
     * Model for the list of PWM pins.
     */
    private DefaultListModel<AbstractPinPanel> pwmPinsModel = new DefaultListModel<>();

    /**
     * Class constructor.
     */
    public ControlPanel() {
        super(Main.APP_NAME + " control panel");
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

        cancelButton.addActionListener(e -> {
            dispose();
            onCancel();
        });
        okButton.addActionListener(e -> {
            System.out.println("Saving user input...");
            settings.setUsbPort((String) portsComboBox.getSelectedItem());
            settings.setIndiPort((int) indiPortField.getValue());
            settings.save();
            dispose();
            onOk();
        });

        portsComboBox.setSelectedItem(settings.getUsbPort());

        addDigitalPinButton.addActionListener(this);
        removeDigitalPinButton.addActionListener(this);
        addPwmPinButton.addActionListener(this);
        removePwmPinButton.addActionListener(this);

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
        indiPortField = new JSpinner(new SpinnerNumberModel(settings.getIndiPort(), 10, 99999, 1));
        digitalPinsList = new JList<>(digitalPinsModel);
        digitalPinsList.setLayoutOrientation(JList.VERTICAL);
        digitalPinsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        digitalPinsList.setCellRenderer(new PinPanelRenderer());
        pwmPinsList = new JList<>(digitalPinsModel);
        pwmPinsList.setLayoutOrientation(JList.VERTICAL);
        pwmPinsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pwmPinsList.setCellRenderer(new PinPanelRenderer());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == addDigitalPinButton) {
            ArduinoPin pin = askNewPin();
            if (pin != null) {
                settings.getDigitalPins().add(pin);
                digitalPinsModel.addElement(new DigitalPinPanel(pin));
            }

        } else if (source == removeDigitalPinButton) {
            AbstractPinPanel item = digitalPinsList.getSelectedValue();
            digitalPinsModel.removeElement(item);
            settings.getDigitalPins().remove(item.getArduinoPin());

        } else if (source == addPwmPinButton) {
            ArduinoPin pin = askNewPin();
            if (pin != null) {
                settings.getPwmPins().add(pin);
                pwmPinsModel.addElement(new PwmPinPanel(pin));
            }

        } else if (source == removePwmPinButton) {
            AbstractPinPanel item = pwmPinsList.getSelectedValue();
            pwmPinsModel.removeElement(item);
            settings.getPwmPins().remove(item.getArduinoPin());
        }
    }

    /**
     * Shows a dialog to user asking for a pin.
     *
     * @return an {@link ArduinoPin} object with the chosen pin.
     */
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

    /**
     * Called when the user clicks "OK".
     */
    protected abstract void onOk();

    /**
     * Called when the user clicks "Cancel".
     */
    protected abstract void onCancel();
}