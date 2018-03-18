package squareboot.astro.allinone;

import squareboot.astro.allinone.io.GenericSerialPort;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
    private JList<ArduinoPin> digitalPinsList;
    /**
     * The list of PWM pins.
     */
    private JList<ArduinoPin> pwmPinsList;
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
     * Button to edit the selected digital pin.
     */
    private JButton editDigitalPinButton;
    /**
     * Button to edit the selected PWM pin.
     */
    private JButton editPwmPinButton;
    /**
     * Timer to refresh the list of available serial ports.
     */
    private Timer refresher = new Timer("Serial ports refresher");
    /**
     * Model for the list of digital pins.
     */
    private DefaultListModel<ArduinoPin> digitalPinsModel;
    /**
     * Model for the list of PWM pins.
     */
    private DefaultListModel<ArduinoPin> pwmPinsModel;

    /**
     * Class constructor.
     */
    public ControlPanel() {
        super(Main.APP_NAME + " control panel");
        setIconImage(Main.logo);
        setContentPane(parent);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                onCancel();
            }
        });

        for (String p : GenericSerialPort.listAvailablePorts()) {
            portsComboBox.addItem(p);
        }
        refresher.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                boolean popupVisible = portsComboBox.isPopupVisible();
                String selectedItem = (String) portsComboBox.getSelectedItem();
                portsComboBox.removeAllItems();
                for (String p : GenericSerialPort.listAvailablePorts()) {
                    portsComboBox.addItem(p);
                }
                if (popupVisible) {
                    SwingUtilities.invokeLater(() -> portsComboBox.showPopup());
                }
                portsComboBox.setSelectedItem(selectedItem);
            }
        }, 2500, 2500);

        cancelButton.addActionListener(e -> {
            dispose();
            onCancel();
        });
        okButton.addActionListener(e -> {
            System.out.println("Saving user input...");
            Settings settings = Main.getSettings();
            settings.setUsbPort((String) portsComboBox.getSelectedItem());
            settings.setIndiPort((int) indiPortField.getValue());
            settings.save();
            dispose();
            onOk();
        });

        portsComboBox.setSelectedItem(Main.getSettings().getUsbPort());

        addDigitalPinButton.addActionListener(this);
        removeDigitalPinButton.addActionListener(this);
        editDigitalPinButton.addActionListener(this);
        addPwmPinButton.addActionListener(this);
        removePwmPinButton.addActionListener(this);
        editPwmPinButton.addActionListener(this);

        setBounds(200, 150, 650, 550);
        setVisible(true);
    }

    @Override
    public void dispose() {
        super.dispose();
        refresher.cancel();
    }

    private void createUIComponents() {
        Settings settings = Main.getSettings();
        indiPortField = new JSpinner(new SpinnerNumberModel(settings.getIndiPort(), 10, 99999, 1));

        digitalPinsModel = new DefaultListModel<>();
        for (ArduinoPin pin : settings.getDigitalPins().toArray()) {
            digitalPinsModel.addElement(pin);
        }
        digitalPinsList = new JList<>(digitalPinsModel);
        digitalPinsList.setLayoutOrientation(JList.VERTICAL);
        digitalPinsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        digitalPinsList.getSelectionModel().addListSelectionListener(e -> {
            boolean b = !((ListSelectionModel) e.getSource()).isSelectionEmpty();
            removeDigitalPinButton.setEnabled(b);
            editDigitalPinButton.setEnabled(b);
        });

        pwmPinsModel = new DefaultListModel<>();
        for (ArduinoPin pin : settings.getPwmPins().toArray()) {
            pwmPinsModel.addElement(pin);
        }
        pwmPinsList = new JList<>(pwmPinsModel);
        pwmPinsList.setLayoutOrientation(JList.VERTICAL);
        pwmPinsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pwmPinsList.getSelectionModel().addListSelectionListener(e -> {
            boolean b = !((ListSelectionModel) e.getSource()).isSelectionEmpty();
            removePwmPinButton.setEnabled(b);
            editPwmPinButton.setEnabled(b);
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        Settings settings = Main.getSettings();
        if (source == addDigitalPinButton) {
            ArduinoPin pin = askNewPin();
            if (pin != null) {
                settings.getDigitalPins().add(pin);
                digitalPinsModel.addElement(pin);
            }

        } else if (source == removeDigitalPinButton) {
            ArduinoPin item = digitalPinsList.getSelectedValue();
            digitalPinsModel.removeElement(item);
            settings.getDigitalPins().remove(item);

        } else if (source == editDigitalPinButton) {
            new DigitalPinDialog(this, digitalPinsList.getSelectedValue());

        } else if (source == addPwmPinButton) {
            ArduinoPin pin = askNewPin();
            if (pin != null) {
                settings.getPwmPins().add(pin);
                pwmPinsModel.addElement(pin);
            }

        } else if (source == removePwmPinButton) {
            ArduinoPin item = pwmPinsList.getSelectedValue();
            pwmPinsModel.removeElement(item);
            settings.getPwmPins().remove(item);

        } else if (source == editPwmPinButton) {
            new PwmPinDialog(this, pwmPinsList.getSelectedValue());
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