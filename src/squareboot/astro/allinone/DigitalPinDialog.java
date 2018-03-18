package squareboot.astro.allinone;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultFormatter;

/**
 * @author SquareBoot
 * @version 0.1
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class DigitalPinDialog extends AbstractPinDialog {

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
    private JCheckBox stateCheckBox;
    /**
     * The label with the pin ID.
     */
    private JSpinner pinSpinner;

    /**
     * Class constructor.
     *
     * @param pin a pin.
     */
    public DigitalPinDialog(JFrame frame, ArduinoPin pin) {
        super(frame, pin);
        setContentPane(parent);

        pinSpinner.addChangeListener(e -> pin.setPin((int) pinSpinner.getValue()));
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
                pin.setName(nameTextField.getText());
            }
        });
        nameTextField.setText(pin.getName());

        stateCheckBox.addActionListener(e -> pin.setValue(stateCheckBox.isSelected() ? 255 : 0));
        stateCheckBox.setSelected(pin.getValue() == 255);

        setLocation(250, 250);
        pack();
        setVisible(true);
    }

    private void createUIComponents() {
        pinSpinner = new JSpinner(new SpinnerNumberModel(13, 2, 99, 1));
        ((DefaultFormatter) ((JFormattedTextField) pinSpinner.getEditor().getComponent(0)).getFormatter())
                .setCommitsOnValidEdit(true);
    }
}