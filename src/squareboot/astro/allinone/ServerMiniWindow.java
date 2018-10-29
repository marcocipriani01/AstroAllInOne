package squareboot.astro.allinone;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Mini window shown in server mode to stop the application (if GUI is enabled).
 *
 * @author SquareBoot
 * @version 1.1
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ServerMiniWindow extends JFrame {

    /**
     * The parent component.
     */
    private JPanel parent;
    /**
     * Click to exit.
     */
    private JButton exitButton;

    /**
     * Class constructor.
     */
    public ServerMiniWindow() {
        super(Main.APP_NAME);
        setIconImage(ControlPanel.APP_LOGO);
        setContentPane(parent);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                exit();
            }
        });
        exitButton.addActionListener(e -> exit());
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