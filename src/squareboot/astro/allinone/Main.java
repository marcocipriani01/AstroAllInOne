package squareboot.astro.allinone;

import org.apache.commons.cli.*;
import squareboot.astro.allinone.indi.INDIArduinoDriver;
import squareboot.astro.allinone.indi.INDIServer;
import squareboot.astro.allinone.io.Arduino;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * The main class of the application.
 *
 * @author SquareBoot
 * @version 0.1
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Main {

    /**
     * The icon (for Swing).
     * */
    public static Image logo;
    static {
        try {
            logo = Toolkit.getDefaultToolkit().getImage(Main.class.
                    getResource("/squareboot/astro/allinone/res/icon.png"));

        } catch (Exception ignored) {

        }
    }
    /**
     * Global settings
     */
    private static Settings settings;
    /**
     * The current running server.
     */
    private static INDIServer server;

    /**
     * @return the current settings.
     */
    public static Settings getSettings() {
        return settings;
    }

    /**
     * @return the current running server.
     */
    public static INDIServer getServer() {
        return server;
    }

    /**
     * Main. Configures the L&F and starts the application.
     */
    public static void main(String[] args) {
        CommandLineParser parser = new DefaultParser();

        Options options = new Options();
        options.addOption("d", "data-dir", true,
                "The directory where AstroAllInOne will retrieve its settings.");
        options.addOption("f", "no-custom-fonts", false,
                "Forces the app to use Java fonts instead of custom one.");
        options.addOption("g", "no-gtk", false,
                "Forces the app to use the Java default L&F.");
        options.addOption("p", "port", true,
                "Specifies a port for the INDI server (default 7624).");

        try {
            CommandLine line = parser.parse(options, args);

            if (!line.hasOption('g')) {
                try {
                    System.out.println("Loading GTK...");
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");

                } catch (Exception e) {
                    System.err.println("Unable to set up GTK: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            if (!line.hasOption('f')) {
                try {
                    System.out.println("Loading fonts...");
                    Font f = loadFont("/squareboot/astro/allinone/res/OpenSans.ttf");
                    UIManager.getLookAndFeelDefaults().put("defaultFont", f.deriveFont(15f));
                    UIManager.getLookAndFeelDefaults().put("InternalFrame.titleFont", f.deriveFont(Font.BOLD).deriveFont(16f));

                } catch (Exception e) {
                    System.err.println("Unable to set up fonts: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            if (line.hasOption('d')) {
                settings = Settings.load(new File(line.getOptionValue('d')));

            } else {
                System.err.println("Please specify option --data-dir " + options.getOption("d").getDescription());
                System.exit(5);
            }

            if (line.hasOption('p')) {
                try {
                    settings.indiPort = Integer.valueOf(line.getOptionValue('p'));

                } catch (NumberFormatException e) {
                    System.err.println("Unable to parse the INDI server port!");
                    System.exit(6);
                }
            }

        } catch (ParseException e) {
            System.err.println("The given arguments are invalid!");
            System.exit(7);
        }

        System.out.println("Starting server...");
        server = new INDIServer(settings.indiPort);
        if (!server.isServerRunning()) {
            System.err.println("Could not start server!");
            System.exit(8);
        }

        System.out.println("Loading Arduino driver...");
        server.loadJava(INDIArduinoDriver.class);
        Arduino arduino = new Arduino(settings.usbPort);
        INDIArduinoDriver arduinoDriver = INDIArduinoDriver.getInstance();
        if (arduinoDriver == null) {
            System.err.println("Due to unknown reasons, the Arduino driver could not be loaded!");
            System.exit(9);
        }
        //arduinoDriver.init(arduino, settings.digitalPins, settings.pwmPins);
    }

    /**
     * Loads a font from a package.
     *
     * @param pck the package where the font is located.
     * @return the required {@code Font} object.
     */
    @SuppressWarnings("SameParameterValue")
    private static Font loadFont(String pck) throws IOException, FontFormatException {
        return Font.createFont(Font.TRUETYPE_FONT, Main.class.getResourceAsStream(pck));
    }
}