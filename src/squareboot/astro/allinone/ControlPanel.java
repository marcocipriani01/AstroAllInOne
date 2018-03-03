package squareboot.astro.allinone;

import squareboot.astro.allinone.indi.DriverDefinition;
import squareboot.astro.allinone.serial.Arduino;

import javax.swing.*;
import java.util.*;
import java.util.Timer;

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
    private JButton refreshButton;
    private JPanel customDriversPanel;
    private JDriversList driversList;
    private ArrayList<String> serialPorts;
    private Timer refresher = new Timer("Serial ports refresher");

    /**
     * Class constructor.
     */
    public ControlPanel() {
        super("AstroAllInOne control panel");
        setContentPane(parent);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
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
            Main.settings.usbPort = (String) portsComboBox.getSelectedItem();
            Main.settings.indiPort = (int) indiPortField.getValue();
            Main.settings.drivers = driversList.getDrivers();
            Main.settings.shutterCablePin = (int) shutterCablePin.getValue();
            Main.settings.save(Main.file);
            dispose();
        });

        portsComboBox.setSelectedItem(Main.settings.usbPort);
        indiPortField.setValue(Main.settings.indiPort);
        for (DriverDefinition dd : Main.settings.drivers) {
            driversList.getDriversModel().addElement(dd);
        }
        shutterCablePin.setValue(Main.settings.shutterCablePin);

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
        customDriversPanel = new JPanel();
        driversList = new JDriversList(this, null);
        customDriversPanel.add(driversList.getPanel());
    }
}