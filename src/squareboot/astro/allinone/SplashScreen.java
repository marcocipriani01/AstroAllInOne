package squareboot.astro.allinone;

import javax.swing.*;
import java.awt.*;

/**
 * Simple splash screen.
 *
 * @author SquareBoot
 * @version 0.1
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class SplashScreen extends JWindow {

    /**
     * Class constructor.
     */
    public SplashScreen() {
        this(3000);
    }

    /**
     * Class constructor.
     *
     * @param duration the duration of the splash screen.
     */
    public SplashScreen(int duration) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        ImageIcon icon = new ImageIcon(SplashScreen.class.getResource("/squareboot/astro/allinone/res/splash.jpg"));
        int iconWidth = icon.getIconWidth();
        int iconHeight = icon.getIconHeight();
        setBounds((screen.width - iconWidth) / 2, (screen.height - iconHeight) / 2, iconWidth, iconHeight);
        JLabel label = new JLabel(icon);
        getContentPane().add(label, BorderLayout.NORTH);
        setVisible(true);
        if (duration > 0) {
            try {
                Thread.sleep(duration);

            } catch (Exception ignored) {

            }
        }
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        setCursor(b ? new Cursor(Cursor.WAIT_CURSOR) : new Cursor(Cursor.DEFAULT_CURSOR));
    }
}