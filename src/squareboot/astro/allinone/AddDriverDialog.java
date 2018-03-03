package squareboot.astro.allinone;

import squareboot.astro.allinone.indi.DriverDefinition;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

/**
 * @author SquareBoot
 * @author thanks to peeskillet
 * @version 0.1
 * @see <a href="http://stackoverflow.com/a/26272327">Filtering a JList from text field input</a>
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class AddDriverDialog extends JDialog {

    private final DriverDefinition[] drivers;
    private JPanel parent;
    private JList<DriverDefinition> driversList;
    private JTextField searchField;
    private JButton addDriverButton;

    /**
     * Class constructor.
     *
     * @param parent the parent frame.
     */
    public AddDriverDialog(JFrame frame, DriverDefinition[] drivers) {
        super(frame, "Add a new driver", Dialog.ModalityType.DOCUMENT_MODAL);
        this.drivers = drivers;
        setContentPane(parent);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }

            private void filter() {
                DefaultListModel<DriverDefinition> model = (DefaultListModel<DriverDefinition>) driversList.getModel();
                for (DriverDefinition dd : drivers) {
                    if (!dd.getIdentifier().toLowerCase().contains(searchField.getText().toLowerCase())) {
                        if (model.contains(dd)) {
                            model.removeElement(dd);
                        }

                    } else {
                        if (!model.contains(dd)) {
                            model.addElement(dd);
                        }
                    }
                }
            }
        });

        addDriverButton.addActionListener(e -> {
            onResult(driversList.getSelectedValue());
            dispose();
        });

        pack();
        setLocation(200, 150);
        setVisible(true);
    }

    /**
     * Called when the user adds a new server.
     */
    protected abstract void onResult(DriverDefinition driver);

    private void createUIComponents() {
        DefaultListModel<DriverDefinition> model = new DefaultListModel<>();
        for (DriverDefinition dd : drivers) {
            model.addElement(dd);
        }
        driversList = new JList<>(model);
        driversList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        driversList.setSelectedIndex(0);
    }
}