package squareboot.astro.allinone;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    private JButton copyFocuserPortToButton;
    private JButton exitButton;

    /**
     * Class constructor.
     */
    public ServerMiniWindow(String focuserPortString) {
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