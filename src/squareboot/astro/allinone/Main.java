package squareboot.astro.allinone;

import org.apache.commons.cli.*;
import squareboot.astro.allinone.indi.INDIArduinoDriver;
import squareboot.astro.allinone.indi.INDIServer;
import squareboot.astro.allinone.io.ConnectionError;
import squareboot.astro.allinone.io.SerialPortMultiplexer;
import squareboot.astro.allinone.io.SimpleSerialPort;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * The main class of the application.
 *
 * @author SquareBoot
 * @version 0.1
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Main {

    /**
     * The name of this application.
     */
    public static final String APP_NAME = "AstroAllInOne";
    /**
     * The icon (for Swing).
     */
    public static Image logo;
    /**
     * Global settings
     */
    private static Settings settings;
    /**
     * The current running server.
     */
    private static INDIServer server;
    /**
     * Serial port multiplexer.
     */
    private static SerialPortMultiplexer multiplexer;
    /**
     * The Arduino pin driver.
     */
    private static INDIArduinoDriver arduinoDriver;
    /**
     * Do not show the control panel.
     */
    private static boolean showGui = false;
    /**
     * Splash screen.
     */
    private static SplashScreen splash;

    static {
        try {
            logo = Toolkit.getDefaultToolkit().getImage(Main.class.
                    getResource("/squareboot/astro/allinone/res/logo.png"));

        } catch (Exception e) {
            System.err.println("Unable to load app image.");
            e.printStackTrace();
        }
    }

    /**
     * Main. Configures the L&F and starts the application.
     */
    public static void main(String[] args) {
        splash = new SplashScreen(0);

        CommandLineParser parser = new DefaultParser();

        Options options = new Options();
        options.addOption("d", "data-dir", true,
                "The directory where AstroAllInOne will retrieve its settings.");
        options.addOption("g", "no-gtk", false,
                "Forces the app to use the Java default L&F.");
        options.addOption("n", "no-gui", false,
                "Do not show the control panel, no GUI.");
        options.addOption("p", "indi-port", true,
                "Specifies a port for the INDI server (default 7625).");
        options.addOption("a", "serial-port", true,
                "Specifies a serial port.");

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

            if (line.hasOption('d')) {
                System.out.println("Loading data...");
                settings = Settings.load(new File(line.getOptionValue('d')));

            } else {
                System.err.println("Please specify option --data-dir " + options.getOption("d").getDescription());
                exit(ExitCodes.NO_DATA_DIR);
            }

            if (line.hasOption('p')) {
                try {
                    settings.setIndiPort(Integer.valueOf(line.getOptionValue('p')));

                } catch (NumberFormatException e) {
                    System.err.println("Unable to parse the INDI server port!");
                    exit(ExitCodes.PARSE_ERROR);
                }
            }

            if (line.hasOption('a')) {
                settings.setUsbPort(line.getOptionValue('s'));
            }

            showGui = !line.hasOption('n');

        } catch (ParseException e) {
            System.err.println("The given arguments are not valid!");
            exit(ExitCodes.PARSE_ERROR);
        }

        if (!showGui) {
            start();

        } else {
            SwingUtilities.invokeLater(() -> {
                splash.setVisible(false);
                new ControlPanel() {
                    @Override
                    protected void onOk() {
                        start();
                    }

                    @Override
                    protected void onCancel() {
                        exit();
                    }
                };
            });
        }
    }

    private static void start() {
        if (showGui) {
            splash.setVisible(true);
        }
        System.out.println("Starting server...");
        server = new INDIServer(settings.getIndiPort());
        try {
            Thread.sleep(200);

        } catch (InterruptedException ignored) {

        }
        if (!server.isServerRunning()) {
            System.err.println("Could not start server!");
            exit(ExitCodes.SERVER_ERROR);
            return;
        }

        System.out.println("Loading Arduino driver...");
        server.loadJava(INDIArduinoDriver.class);
        SimpleSerialPort realArduino;
        try {
            realArduino = new SimpleSerialPort(settings.getUsbPort());

        } catch (ConnectionError e) {
            System.err.println("Unable to connect to the given serial port!");
            if (showGui) {
                JOptionPane.showMessageDialog(null, "Unable to connect to the given serial port!",
                        APP_NAME, JOptionPane.ERROR_MESSAGE);
            }
            exit(ExitCodes.ARDUINO_ERROR);
            return;
        }
        arduinoDriver = INDIArduinoDriver.getInstance();
        if (arduinoDriver == null) {
            System.err.println("Due to unknown reasons, the Arduino driver could not be loaded!");
            if (showGui) {
                JOptionPane.showMessageDialog(null, "Due to unknown reasons, the Arduino driver could not be loaded!",
                        APP_NAME, JOptionPane.ERROR_MESSAGE);
            }
            exit(ExitCodes.ARDUINO_ERROR);
            return;
        }
        try {
            arduinoDriver.init(realArduino, settings.getDigitalPins().toArray(), settings.getPwmPins().toArray());

        } catch (IllegalStateException e) {
            System.err.println(e.getMessage());
            if (showGui) {
                JOptionPane.showMessageDialog(splash, e.getMessage(), APP_NAME, JOptionPane.ERROR_MESSAGE);
            }
            Main.exit(ExitCodes.ARDUINO_ERROR);
            return;
        }

        try {
            System.out.println("Loading port forwarder (socat)...");
            multiplexer = new SerialPortMultiplexer(realArduino);

        } catch (IllegalStateException e) {
            System.err.println("socat error!");
            if (showGui) {
                JOptionPane.showMessageDialog(null, "socat error!",
                        APP_NAME, JOptionPane.ERROR_MESSAGE);
            }
            Main.exit(Main.ExitCodes.SOCAT_ERROR);
            return;
        }

        System.out.println("Starting status window...");
        SwingUtilities.invokeLater(() -> {
            splash.dispose();
            splash = null;
            new ServerMiniWindow(multiplexer.getMockedPort(), arduinoDriver);
        });
    }

    /**
     * @return the multiplexer.
     * @see SerialPortMultiplexer
     */
    public static SerialPortMultiplexer getMultiplexer() {
        return multiplexer;
    }

    /**
     * @return the stored Arduino pin driver.
     * @see INDIArduinoDriver
     */
    public static INDIArduinoDriver getArduinoDriver() {
        return arduinoDriver;
    }

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
     * Closes the app.
     *
     * @param code an exit code.
     */
    public static void exit(int code) {
        System.out.println("Bye!");
        if (server != null && server.isServerRunning()) {
            server.stopServer();
        }
        if (multiplexer != null) {
            multiplexer.stop();
        }
        System.exit(code);
    }

    /**
     * Closes the app.
     *
     * @param code an exit code.
     */
    public static void exit(ExitCodes code) {
        exit(code.getCode());
    }

    /**
     * Closes the app.
     */
    public static void exit() {
        exit(0);
    }

    /**
     * A list of common exit codes.
     *
     * @author SquareBoot
     * @version 0.1
     */
    public enum ExitCodes {
        NO_DATA_DIR(5),
        PARSE_ERROR(6),
        SERVER_ERROR(7),
        ARDUINO_ERROR(8),
        SOCAT_ERROR(9);

        /**
         * The exit code.
         */
        private int code;

        /**
         * Enum constructor.
         */
        ExitCodes(int code) {
            this.code = code;
        }

        /**
         * @return the stored exit code.
         */
        public int getCode() {
            return code;
        }
    }
}