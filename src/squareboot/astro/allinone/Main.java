package squareboot.astro.allinone;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import org.apache.commons.cli.*;
import squareboot.astro.allinone.indi.INDIServer;
import squareboot.astro.allinone.io.GenericSerialPort;

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
     * App default font.
     */
    public static Font APP_BASE_FONT;
    /**
     * App title font.
     */

    public static Font TITLE_FONT;
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
                    APP_BASE_FONT = f.deriveFont(15f);
                    UIManager.getLookAndFeelDefaults().put("defaultFont", APP_BASE_FONT);
                    TITLE_FONT = f.deriveFont(Font.BOLD).deriveFont(16f);
                    UIManager.getLookAndFeelDefaults().put("InternalFrame.titleFont", TITLE_FONT);

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
            System.exit(8);
        }

        new GenericSerialPort("/dev/pts/3") {

            @Override
            protected int getEventMask() {
                return SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;
                /*return SerialPort.MASK_RXCHAR +
                        SerialPort.MASK_CTS +
                        SerialPort.MASK_DSR +
                        SerialPort.MASK_BREAK +
                        SerialPort.MASK_RING +
                        SerialPort.MASK_RLSD +
                        SerialPort.MASK_RXFLAG +
                        SerialPort.MASK_TXEMPTY +
                        SerialPort.MASK_ERR;*/
            }

            @Override
            public void serialEvent(SerialPortEvent portEvent) {
                System.err.println("PEPPE! " + portEvent.getEventType());
            }
        };

        /*System.out.println("Starting server...");
        server = new INDIServer(settings.indiPort);
        if (!server.isServerRunning()) {
            System.err.println("Could not start server!");
            System.exit(9);
        }

        System.out.println("Loading Arduino driver...");
        server.loadJava(INDIArduinoDriver.class);
        Arduino arduino = new Arduino(settings.usbPort);
        INDIArduinoDriver arduinoDriver = INDIArduinoDriver.getInstance();
        if (arduinoDriver == null) {
            System.err.println("Due to unknown reasons, the Arduino driver could not be loaded!");
            System.exit(10);
        }
        arduinoDriver.init(arduino, new ArduinoPin[]{
                        new ArduinoPin(122, "c2", 122)},
                new ArduinoPin[]{
                        new ArduinoPin(12, "c", 12)});*/
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