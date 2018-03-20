package squareboot.astro.allinone;

import squareboot.astro.allinone.indi.INDIArduinoDriver;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author SquareBoot
 * @version 0.1
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ServerMiniWindow extends JFrame {

    /**
     * The parent component.
     */
    private JPanel parent;
    /**
     * Click to copy the virtual port of the focuser.
     */
    private JButton copyFocuserPortToButton;
    /**
     * Click to exit.
     */
    private JButton exitButton;
    /**
     * Click to force the Arduino reboot (pay attention! you may modify the focuser state unexpectedly!).
     */
    private JButton forceRebootButton;

    /**
     * Class constructor.
     */
    public ServerMiniWindow(String focuserPortString, INDIArduinoDriver arduinoDriver) {
        super(Main.APP_NAME);
        setIconImage(Main.logo);
        setContentPane(parent);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                exit();
            }
        });
        exitButton.addActionListener(e -> exit());
        copyFocuserPortToButton.addActionListener(e -> {
            StringSelection toClipboard = new StringSelection(focuserPortString);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(toClipboard, toClipboard);
        });
        forceRebootButton.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(ServerMiniWindow.this,
                    "Are you sure of rebooting Arduino? Pay attention! you may modify the focuser state unexpectedly!",
                    Main.APP_NAME, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                arduinoDriver.forceReboot();
            }
        });

        pack();
        setLocation(250, 250);
        setVisible(true);
    }

    private void exit() {
        if (JOptionPane.showConfirmDialog(ServerMiniWindow.this, "Are you sure?",
                Main.APP_NAME, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            Main.exit();
        }
    }
}