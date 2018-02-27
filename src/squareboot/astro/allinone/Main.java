package squareboot.astro.allinone;

import laazotea.indi.driver.INDIConnectionHandler;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;

/**
 * @author SquareBoot
 * @version 0.1
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Main {

    /**
     * App default font.
     */
    public static Font APP_BASE_FONT;
    /**
     * App title font.
     */
    public static Font TITLE_FONT;
    /**
     * Console font.
     */
    public static Font CONSOLE_FONT;

    static {
        try {
            APP_BASE_FONT = loadFont("/squareboot/astro/allinone/res/OpenSans.ttf").deriveFont(15f);
            TITLE_FONT = loadFont("/squareboot/astro/allinone/res/OpenSans.ttf").deriveFont(Font.BOLD).deriveFont(16f);
            CONSOLE_FONT = loadFont("/squareboot/astro/allinone/res/CutiveMono.ttf").deriveFont(15f);

        } catch (Exception e) {
            System.err.println("Unable to set up the custom fonts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Main. Configures the Look and Feel and starts the application.
     *
     * @see <a href="https://github.com/khuxtable/seaglass/releases/tag/seaglasslookandfeel-0.2.1">Seaglass L&F - GitHub</a>
     * @see <a href="https://mvnrepository.com/artifact/com.seaglasslookandfeel/seaglasslookandfeel/0.2.1">Seaglass L&f - Maven repository</a>
     * @see <a href="http://stackoverflow.com/questions/9123002/how-to-install-configure-custom-java-look-and-feel">How to install/configure custom Java Look-And-Feel?</a>
     */
    public static void main(String[] args) {
        System.err.println("Welcome!");
        /*try {
            System.err.println("Setting up Seaglass L&F...");
            //UIManager.setLookAndFeel("com.seaglasslookandfeel.SeaGlassLookAndFeel");

            UIManager.getLookAndFeelDefaults()
                    .put("defaultFont", APP_BASE_FONT);
            UIManager.getLookAndFeelDefaults()
                    .put("InternalFrame.titleFont", TITLE_FONT);

        } catch (Exception e) {
            System.err.println("Unable to set up UI correctly!");
            e.printStackTrace();
        }*/

        INDIPWMDriver driver = new INDIPWMDriver(System.in, System.out);
        driver.startListening();
    }

    /**
     * Loads a font from a package.
     *
     * @param pck the package where the font is located.
     * @return the required {@code Font} Object.
     */
    private static Font loadFont(String pck) throws IOException, FontFormatException {
        return Font.createFont(Font.TRUETYPE_FONT, Main.class.getResourceAsStream(pck));
    }
}