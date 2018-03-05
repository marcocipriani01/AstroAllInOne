package squareboot.astro.allinone;

import squareboot.astro.allinone.indi.DriverDefinition;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author SquareBoot
 * @version 0.1
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class JDriversList implements ListSelectionListener {

    private JButton addDriverButton;
    private JButton removeDriverButton;
    private JList<DriverDefinition> driversList;
    private JPanel parent;
    private DefaultListModel<DriverDefinition> driversModel;

    /**
     * Class constructor.
     */
    public JDriversList(JFrame frame, DriversListListener listener) {
        addDriverButton.addActionListener(e -> new AddDriverDialog(frame, ) {
            @Override
            protected void onResult(DriverDefinition driver) {
                if (driversModel.contains(driver)) {
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
                int index = driversList.getSelectedIndex();
                if (index == -1) {
                    index = 0;

                } else {
                    index++;
                }
                driversModel.insertElementAt(driver, index);
                driversList.setSelectedIndex(index);
                driversList.ensureIndexIsVisible(index);
                if (listener != null) {
                    listener.onDriverAdd(driver);
                }
            }
        });
        removeDriverButton.addActionListener(e -> {
            int index = driversList.getSelectedIndex();
            DriverDefinition driver = driversList.getSelectedValue();
            driversModel.remove(index);
            int size = driversModel.getSize();
            if (size == 0) {
                removeDriverButton.setEnabled(false);

            } else {
                if (index == driversModel.getSize()) {
                    index--;
                }
                driversList.setSelectedIndex(index);
                driversList.ensureIndexIsVisible(index);
            }
            if (listener != null) {
                listener.onDriverRemove(driver);
            }
        });
    }

    /**
     * Adds the specified component to the model.
     *
     * @param element the component to be added.
     */
    public void addElement(DriverDefinition element) {
        driversModel.addElement(element);
    }

    public ArrayList<DriverDefinition> getDrivers() {
        Object[] drivers = driversModel.toArray();
        ArrayList<DriverDefinition> definitions = new ArrayList<>();
        Collections.addAll(definitions, Arrays.copyOf(drivers, drivers.length, DriverDefinition[].class));
        return definitions;
    }

    public DefaultListModel<DriverDefinition> getDriversModel() {
        return driversModel;
    }

    /**
     * @return the stored {@link JPanel}.
     */
    public JPanel getPanel() {
        return parent;
    }

    private void createUIComponents() {
        driversModel = new DefaultListModel<>();
        driversList = new JList<>(driversModel);
        driversList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        driversList.setSelectedIndex(0);
        driversList.addListSelectionListener(this);
        driversList.setVisibleRowCount(7);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            if (driversList.getSelectedIndex() == -1) {
                removeDriverButton.setEnabled(false);

            } else {
                removeDriverButton.setEnabled(true);
            }
        }
    }
}