package squareboot.astro.allinone;

import org.apache.commons.cli.*;
import squareboot.astro.allinone.indi.INDIArduinoDriver;
import squareboot.astro.allinone.indi.INDIServer;

import javax.swing.*;
import java.io.File;
import java.io.PrintWriter;

/**
 * The main class of the application. Interprets the input commands and
 * runs the server, the driver or the control panel.
 *
 * @author SquareBoot
 * @version 2.0
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Main {

    /**
     * The name of this application.
     */
    public static final String APP_NAME = "AstroAllInOne";
    /**
     * Verbose/debug mode.
     */
    private static boolean verboseMode = false;
    /**
     * Global settings
     */
    private static Settings settings;
    /**
     * The current running server.
     */
    private static INDIServer server;
    /**
     * Do not show the control panel.
     */
    private static boolean showGUI = false;

    /**
     * Main. Configures the L&F and starts the application.
     */
    public static void main(String[] args) {
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        Option settingsOption = new Option("s", "settings", true,
                "The directory where AstroAllInOne will save and retrieve its settings.");
        settingsOption.setRequired(true);
        options.addOption(settingsOption);
        options.addOption("c", "control-panel", false,
                "Shows the control panel.");
        options.addOption("d", "driver", false,
                "Driver-only mode (no server, stdin/stdout)");
        options.addOption("p", "indi-port", true,
                "Specifies a port for the INDI server (default 7625) and switches to stand-alone mode, with server.");
        options.addOption("a", "serial-port", true,
                "Specifies a serial port and connects to it if possible. Otherwise it will be stored to settings only.");
        options.addOption("v", "verbose", false, "Verbose mode.");

        boolean autoConnectSerial = false;

        try {
            CommandLine line = parser.parse(options, args);

            verboseMode = line.hasOption('v');

            if (line.hasOption('s')) {
                if (verboseMode) {
                    System.err.println("Loading data...");
                }
                settings = Settings.load(new File(line.getOptionValue('s')));

            } else {
                exit(ExitCodes.NO_DATA_DIR);
            }

            boolean controlPanel = showGUI = line.hasOption('c'),
                    serverMode = line.hasOption('p'),
                    driverOnly = line.hasOption('d');

            if (line.hasOption('a')) {
                settings.setUsbPort(line.getOptionValue('a'));
                autoConnectSerial = true;
            }

            int serverPort = settings.getIndiPort();
            if (serverMode) {
                try {
                    serverPort = Integer.valueOf(line.getOptionValue('p'));
                    settings.setIndiPort(serverPort);
                    settings.save();

                } catch (NumberFormatException e) {
                    exit(ExitCodes.PARSING_ERROR);
                }
            }

            if (controlPanel && !driverOnly) {
                showGUI = true;
                if (System.getProperty("os.name").toLowerCase().equals("linux")) {
                    try {
                        err("Setting up GTK...");
                        UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");

                    } catch (Exception e) {
                        err("Unable to set-up the GTK l&f", e, false);
                    }
                }
                SwingUtilities.invokeLater(() -> new ControlPanel() {
                    @Override
                    protected void onRunServer(int port) {
                        runServer(port);
                    }
                });

            } else if (serverMode && (!controlPanel && !driverOnly)) {
                runServer(serverPort);

            } else if (driverOnly && (!controlPanel && !serverMode)) {
                new INDIArduinoDriver(System.in, System.out, autoConnectSerial);

            } else {
                exit(ExitCodes.INVALID_OPTIONS);
            }

        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(new PrintWriter(System.err), HelpFormatter.DEFAULT_WIDTH, "astroallinone",
                    "Help for AstroAllInOne:", options, HelpFormatter.DEFAULT_LEFT_PAD,
                    HelpFormatter.DEFAULT_DESC_PAD, "-d -c" +
                            "\n\nDistributed under the Apache License, Version 2.0, by SquareBoot");
            exit(ExitCodes.PARSING_ERROR);
        }
    }

    /**
     * Runs the server with the INDI driver.
     */
    private static void runServer(int port) {
        server = new INDIServer(port);
        server.loadJava(INDIArduinoDriver.class);
    }

    /**
     * @return {@code true} if the user enabled the verbose/debug mode.
     */
    public static boolean isVerboseMode() {
        return verboseMode;
    }

    /**
     * @return {@code true} if the GUI is enabled.
     */
    public static boolean isGUIEnabled() {
        return showGUI;
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

    // ----------- Exit management -----------

    /**
     * Closes the app.
     *
     * @param code an exit code.
     */
    public static void exit(int code) {
        exit(ExitCodes.fromCode(code));
    }

    /**
     * Closes the app.
     *
     * @param code an exit code.
     */
    public static void exit(ExitCodes code) {
        if (server != null && server.isServerRunning()) {
            server.stopServer();
        }
        String message = code.getMessage();
        if (message != null) {
            err(message, true);
        }
        System.exit(code.getCode());
    }

    /**
     * Closes the app.
     */
    public static void exit() {
        exit(0);
    }

    /**
     * Reports an error to the user.
     *
     * @param msg        a message that will be printed to {@link System#err} if verbose mode is on
     * @param e          the exception you want to print the stacktrace of.
     * @param showDialog set to true to show a visual dialog with the same message.
     */
    public static void err(String msg, Exception e, boolean showDialog) {
        if (verboseMode) {
            System.err.println(msg);
            e.printStackTrace();
        }
        if (showGUI && showDialog) {
            JOptionPane.showMessageDialog(null, msg, APP_NAME, JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Reports an error to the user.
     *
     * @param msg        a message that will be printed to {@link System#err} if verbose mode is on
     * @param showDialog set to true to show a visual dialog with the same message.
     */
    public static void err(String msg, boolean showDialog) {
        if (verboseMode) {
            System.err.println(msg);
        }
        if (showGUI && showDialog) {
            JOptionPane.showMessageDialog(null, msg, APP_NAME, JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Reports an error to the user.
     *
     * @param msg a message that will be printed to {@link System#err} if verbose mode is on
     */
    public static void err(String msg) {
        err(msg, false);
    }

    /**
     * Reports an info to the user.
     *
     * @param msg        a message that will be printed to {@link System#err} ({@link System#out} may have been used for the INDI driver).
     * @param showDialog set to true to show a visual dialog with the same message.
     */
    public static void info(String msg, boolean showDialog) {
        System.err.println(msg);
        if (showGUI && showDialog) {
            JOptionPane.showMessageDialog(null, msg, APP_NAME, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Reports an info to the user.
     *
     * @param msg a message that will be printed to {@link System#err} ({@link System#out} may have been used for the INDI driver).
     */
    public static void info(String msg) {
        System.err.println(msg);
    }

    /**
     * A list of common exit codes.
     *
     * @author SquareBoot
     * @version 0.1
     */
    public enum ExitCodes {
        OK(0),
        NO_DATA_DIR(5, "Please add option --settings=/path/to/settings!"),
        PARSING_ERROR(6, "Unable to parse the INDI server port!"),
        INVALID_OPTIONS(7),
        SERVER_ERROR(8, "Could not start server!"),
        ARDUINO_ERROR(9, "Unable to connect to the serial port!"),
        SOCAT_ERROR(10, "socat error!"),
        INTERNAL_ERROR(11, "Internal unexpected error!");

        /**
         * The exit code.
         */
        private int code;
        /**
         * A message.
         */
        private String message;

        /**
         * Enum constructor.
         *
         * @param code integer, exit code.
         */
        ExitCodes(int code) {
            this(code, null);
        }

        /**
         * Enum constructor.
         *
         * @param code    integer, exit code.
         * @param message a message.
         */
        ExitCodes(int code, String message) {
            this.code = code;
            this.message = message;
        }

        /**
         * @return the right {@link ExitCodes} object associated to the given exit code.
         * {@code null} if nothing matches the given code.
         */
        public static ExitCodes fromCode(int code) {
            for (ExitCodes c : values()) {
                if (c.code == code) {
                    return c;
                }
            }
            return null;
        }

        /**
         * @return a message to be printed in case of error. Can be null.
         */
        public String getMessage() {
            return message;
        }

        /**
         * @return the stored exit code.
         */
        public int getCode() {
            return code;
        }
    }
}