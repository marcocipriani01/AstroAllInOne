package squareboot.astro.allinone;

import com.jcraft.jsch.UserInfo;
import squareboot.astro.allinone.io.ConnectionException;
import squareboot.astro.allinone.io.ScpUploader;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.UncheckedIOException;

/**
 * The control panel.
 *
 * @author SquareBoot
 * @version 1.0
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
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                int operation = JOptionPane.showConfirmDialog(null, "Save and exit, exit or cancel?", Main.APP_NAME,
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (operation == JOptionPane.YES_OPTION) {
                    saveConfig();
                    Main.exit(Main.ExitCodes.OK);

                } else if (operation == JOptionPane.NO_OPTION) {
                    Main.exit(Main.ExitCodes.OK);
                }
            }
        });

        saveButton.addActionListener(e -> saveConfig());
        sendConfigButton.addActionListener(e -> {
            Settings settings = saveConfig();
            if (settings != null) {
                String host = JOptionPane.showInputDialog(this, "Remote SSH server IP/address:", "SCP", JOptionPane.QUESTION_MESSAGE),
                        user = JOptionPane.showInputDialog(this, "Remote username:", "SCP", JOptionPane.QUESTION_MESSAGE);
                try {
                    ScpUploader.send(settings.getFile(), user, host, "/home/" + user + "/.config/AstroAllInOne/Settings.json", new UserInfoProvider(this));
                    Main.info("Settings uploaded to remote host!", this);

                } catch (ConnectionException ex) {
                    Main.err(ex.getMessage(), ex, this);
                }
            }
        });
        runServerButton.addActionListener(e -> {
            Settings settings = saveConfig();
            if (settings != null) {
                onRunServer(settings.getIndiPort());
                dispose();
            }
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
    private Settings saveConfig() {
        Main.err("Saving settings...");
        Settings settings = Main.getSettings();
        int indiPort = (int) indiPortField.getValue();
        if (indiPort < 50) {
            Main.err("Invalid INDI port!", this);
            return null;

        } else {
            settings.setIndiPort(indiPort);
        }
        try {
            if (!PinArray.checkPins(settings.getDigitalPins().toArray(), settings.getPwmPins().toArray())) {
                Main.err("Duplicated pins found, please fix this in order to continue.", this);
                return null;
            }

        } catch (IndexOutOfBoundsException e) {
            Main.err(e.getMessage(), e, this);
            return null;
        }
        try {
            settings.save();

        } catch (UncheckedIOException e) {
            e.printStackTrace();
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
     * @return an {@link ArduinoPin} object representing the given pin, or {@code null}.
     */
    private ArduinoPin askNewPin() {
        boolean check = true;
        int pin = -1;
        do {
            try {
                String input = JOptionPane.showInputDialog(this, "New pin",
                        "Control panel", JOptionPane.QUESTION_MESSAGE);
                if (input == null) {
                    return null;
                }
                pin = Integer.valueOf(input);
                if ((pin < 2) || (pin > 99)) {
                    Main.err("Invalid pin: " + pin + "\" is outside the allowed bounds (2 ≤ pin ≤ 99)!", this);

                } else {
                    check = false;
                }

            } catch (NumberFormatException e) {
                Main.err("Invalid pin! Must be a number.", this);
            }
        } while (check);
        return new ArduinoPin(pin, "Pin " + pin);
    }

    /**
     * Invoked when the user wants to run the server.
     *
     * @param port the port of the server.
     */
    protected abstract void onRunServer(int port);

    /**
     * Abstract {@link JDialog} to ask the user for a pin, its name and its value.
     *
     * @author SquareBoot
     * @version 1.0
     * @see DigitalPinDialog
     * @see PwmPinDialog
     */
    public abstract static class AbstractPinDialog extends JDialog {

        /**
         * An Arduino pin.
         */
        protected ArduinoPin pin;

        /**
         * Class constructor.
         *
         * @param pin a pin.
         */
        public AbstractPinDialog(JFrame frame, ArduinoPin pin) {
            super(frame, "Pin editor", ModalityType.DOCUMENT_MODAL);
            setIconImage(APP_LOGO);
            this.pin = pin;
        }

        protected void setUpPinFields(JSpinner pinSpinner, JTextField nameTextField) {
            pinSpinner.addChangeListener(e -> this.pin.setPin((int) pinSpinner.getValue()));
            pinSpinner.setValue(pin.getPin());

            nameTextField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void changedUpdate(DocumentEvent e) {
                    updateName();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    updateName();
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    updateName();
                }

                public void updateName() {
                    AbstractPinDialog.this.pin.setName(nameTextField.getText());
                }
            });
            nameTextField.addActionListener(e -> dispose());
            nameTextField.setText(pin.getName());
        }

        protected void showUp() {
            setLocation(250, 250);
            pack();
            setVisible(true);
        }

        /**
         * @return the stored pin.
         */
        public ArduinoPin getArduinoPin() {
            return pin;
        }
    }

    /**
     * User info provider.
     *
     * @author SquareBoot
     * @author JCraft
     * @version 1.0
     * @see UserInfo
     */
    public static class UserInfoProvider implements UserInfo {

        String password;
        private JFrame parentWindow;

        /**
         * Class constructor.
         *
         * @param parentWindow parent window for dialogs.
         */
        public UserInfoProvider(JFrame parentWindow) {
            this.parentWindow = parentWindow;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public boolean promptYesNo(String str) {
            return JOptionPane.showConfirmDialog(parentWindow, str, "SCP",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
        }

        @Override
        public String getPassphrase() {
            return null;
        }

        @Override
        public boolean promptPassphrase(String message) {
            return true;
        }

        @Override
        public boolean promptPassword(String message) {
            JTextField passwordField = new JPasswordField(20);
            if (JOptionPane.showConfirmDialog(parentWindow, new Object[]{passwordField},
                    message, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION) {
                password = passwordField.getText();
                return true;

            } else {
                return false;
            }
        }

        @Override
        public void showMessage(String message) {
            JOptionPane.showMessageDialog(parentWindow, message, "SCP", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}