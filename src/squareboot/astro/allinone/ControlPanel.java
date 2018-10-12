package squareboot.astro.allinone;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.UncheckedIOException;

/**
 * The control panel.
 *
 * @author SquareBoot
 * @version 0.1
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class ControlPanel extends JFrame implements ActionListener {

    /**
     * Application icon (for swing).
     */
    public static Image APP_LOGO = Toolkit.getDefaultToolkit().getImage(ControlPanel.class.
            getResource("/squareboot/astro/allinone/logo.png"));

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
     * Button to save the pin config.
     */
    private JButton saveButton;
    /**
     * Button to run the stand-alone INDI server.
     */
    private JButton runServerButton;
    /**
     * Button to send the pin config to another computer.
     */
    private JButton sendConfigButton;

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
        setIconImage(APP_LOGO);
        setContentPane(parent);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        saveButton.addActionListener(e -> {
            savePinConfig();
            dispose();
        });
        sendConfigButton.addActionListener(e -> {
            Settings settings = savePinConfig();
            //settings.getFile();
            //TODO send config
        });
        runServerButton.addActionListener(e -> {
            onRunServer();
            dispose();
        });

        addDigitalPinButton.addActionListener(this);
        removeDigitalPinButton.addActionListener(this);
        editDigitalPinButton.addActionListener(this);
        digitalPinsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editDigitalPin();
                }
            }
        });
        addPwmPinButton.addActionListener(this);
        removePwmPinButton.addActionListener(this);
        editPwmPinButton.addActionListener(this);
        pwmPinsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editPwmPin();
                }
            }
        });

        setBounds(200, 150, 600, 500);
        setVisible(true);
    }


    /**
     * Sets up the user interface.
     */
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

    /**
     * Saves all the configuration to the settings.
     *
     * @return the used {@link Settings} object. May be null if the saving failed.
     */
    private Settings savePinConfig() {
        Main.err("Saving user input...");
        int indiPort = (int) indiPortField.getValue();
        if (indiPort < 50) {
            Main.err("Invalid INDI port!", true);
            return null;
        }
        Settings settings = Main.getSettings();
        settings.setIndiPort(indiPort);
        try {
            if (PinArray.checkPins(settings.getDigitalPins().toArray(), settings.getPwmPins().toArray())) {
                Main.err("Duplicated pins found, please fix this in order to continue.", true);
                return null;
            }

        } catch (IndexOutOfBoundsException e) {
            Main.err(e.getMessage(), e, true);
            return null;
        }
        try {
            settings.save();

        } catch (UncheckedIOException e) {
            return null;
        }
        return settings;
    }

    /**
     * Add/remove/edit digital and PWM pins button actions.
     */
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
            editDigitalPin();

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
            editPwmPin();
        }
    }

    /**
     * Shows a dialog to edit the selected PWM pin.
     */
    private void editPwmPin() {
        new PwmPinDialog(this, pwmPinsList.getSelectedValue());
        pwmPinsList.repaint();
    }

    /**
     * Shows a dialog to edit the selected digital pin.
     */
    private void editDigitalPin() {
        new DigitalPinDialog(this, digitalPinsList.getSelectedValue());
        digitalPinsList.repaint();
    }

    /**
     * Shows a dialog to the user asking for a new pin's number.
     *
     * @return an {@link ArduinoPin} object representing the given pin.
     */
    private ArduinoPin askNewPin() {
        boolean check = true;
        int value = -1;
        do {
            try {
                String input = JOptionPane.showInputDialog(this, "New pin",
                        "Control panel", JOptionPane.QUESTION_MESSAGE);
                if (input == null) {
                    return null;
                }
                value = Integer.valueOf(input);
                check = false;

            } catch (NumberFormatException e) {
                Main.err("Invalid pin! Must be a number.", true);
            }
        } while (check);
        return new ArduinoPin(value, "Pin " + value);
    }

    /**
     * Invoked when the user wants to run the server.
     */
    protected abstract void onRunServer();
}