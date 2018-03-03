package squareboot.astro.allinone;

import com.sun.java.swing.plaf.gtk.GTKLookAndFeel;
import laazotea.indi.driver.INDIDriver;
import squareboot.astro.allinone.indi.*;
import squareboot.astro.allinone.serial.Arduino;
import squareboot.astro.allinone.serial.ArduinoPin;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

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
    /**
     * Global settings
     * */
    public static Settings settings;

    static {
        try {
            APP_BASE_FONT = loadFont("/squareboot/astro/allinone/res/OpenSans.ttf").deriveFont(15f);
            TITLE_FONT = loadFont("/squareboot/astro/allinone/res/OpenSans.ttf").deriveFont(Font.BOLD).deriveFont(16f);
            CONSOLE_FONT = loadFont("/squareboot/astro/allinone/res/CutiveMono.ttf").deriveFont(15f);

        } catch (Exception e) {
            System.err.println("Unable to set up fonts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static File file;

    public static DriverDefinition[] drivers;

    /**
     * Main. Configures the Look and Feel and starts the application.
     *
     * @see <a href="https://github.com/khuxtable/seaglass/releases/tag/seaglasslookandfeel-0.2.1">Seaglass L&F - GitHub</a>
     * @see <a href="https://mvnrepository.com/artifact/com.seaglasslookandfeel/seaglasslookandfeel/0.2.1">Seaglass L&f - Maven repository</a>
     * @see <a href="http://stackoverflow.com/questions/9123002/how-to-install-configure-custom-java-look-and-feel">How to install/configure custom Java Look-And-Feel?</a>
     */
    public static void main(String[] args) {
        try {
            System.out.println("Loading fonts...");
            if (APP_BASE_FONT != null) {
                UIManager.getLookAndFeelDefaults().put("defaultFont", APP_BASE_FONT);
            }
            if (TITLE_FONT != null) {
                UIManager.getLookAndFeelDefaults().put("InternalFrame.titleFont", TITLE_FONT);
            }
            System.out.println("Loading GTK+...");
            UIManager.setLookAndFeel(new GTKLookAndFeel());

        } catch (Exception e) {
            System.err.println("Unable to set up UI correctly!");
            e.printStackTrace();
        }

        System.out.println("Starting server...");
        INDIServer server = new INDIServer();
        System.out.println("Loading drivers...");
        server.loadJava(INDIArduinoDriver.class);

        // find /usr/bin -name indi_* -perm /u+x -type f > "$(dirname "$0")/drivers"
       /*File[] list = new File("/usr/bin").listFiles();
        ArrayList<DriverDefinition> driverDefinitions = new ArrayList<>();
        for (File f : list) {
            if (f.getName().startsWith("indi_") && f.isFile()) {
                driverDefinitions.add(new NativeDriverDefinition(f.getAbsolutePath(), f.getAbsolutePath()));
                System.out.println(f.getName());
            }
        }
        Object[] array = driverDefinitions.toArray();
        drivers = Arrays.copyOf(array, array.length, DriverDefinition[].class);*/

        //Arduino arduino = new Arduino("/dev/ttyACM0");
        INDIArduinoDriver arduinoDriver = INDIArduinoDriver.getInstance();
        if (arduinoDriver == null) {
            System.out.println("peppe");
        }
        //arduinoDriver.init(arduino, new ArduinoPin[]{new ArduinoPin(122, "c2", 122)}, new ArduinoPin[]{new ArduinoPin(12, "c", 12)});
        file = new File(args[0]);
        //settings = Settings.load(file);
        settings = Settings.empty();
        settings.drivers.add(new JavaDriverDefinition(arduinoDriver, "peppe"));
        settings.save(file);
        settings = Settings.load(file);
        //new ControlPanel();
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