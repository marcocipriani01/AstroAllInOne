package squareboot.astro.allinone;

import org.apache.commons.cli.*;
import squareboot.astro.allinone.indi.INDIArduinoDriver;
import squareboot.astro.allinone.indi.INDIServer;
import squareboot.astro.allinone.io.Arduino;
import squareboot.astro.allinone.io.SerialMessageListener;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
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
     * Socat.
     */
    private static SocatRunner socat;
    /**
     * Run to close the system tray.
     */
    private static Runnable disposeTray;

    static {
        try {
            logo = Toolkit.getDefaultToolkit().getImage(Main.class.
                    getResource("/squareboot/astro/allinone/res/icon.png"));

        } catch (Exception e) {
            System.err.println("Unable to load app image.");
            e.printStackTrace();
        }
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

        } catch (ParseException e) {
            System.err.println("The given arguments are invalid!");
            exit(ExitCodes.PARSE_ERROR);
        }

        new ControlPanel() {
            @Override
            protected void onOk() {
                System.out.println("Starting server...");
                server = new INDIServer(settings.getIndiPort());
                if (!server.isServerRunning()) {
                    System.err.println("Could not start server!");
                    exit(ExitCodes.SERVER_ERROR);
                }

                System.out.println("Loading Arduino driver...");
                server.loadJava(INDIArduinoDriver.class);
                Arduino realArduino = new Arduino(settings.getUsbPort());
                INDIArduinoDriver arduinoDriver = INDIArduinoDriver.getInstance();
                if (arduinoDriver == null) {
                    System.err.println("Due to unknown reasons, the Arduino driver could not be loaded!");
                    exit(ExitCodes.ARDUINO_ERROR);
                    return;
                }
                arduinoDriver.init(realArduino, settings.getDigitalPins().toArray(), settings.getPwmPins().toArray());

                System.out.println("Loading port forwarder (socat)...");
                socat = new SocatRunner();
                Thread thread = new Thread(socat);
                thread.start();
                // Wait for process to start
                try {
                    Thread.sleep(50);

                } catch (InterruptedException ignored) {

                }
                if (!socat.isReady()) {
                    System.err.println("socat error!");
                    exit(ExitCodes.SOCAT_ERROR);
                }
                Arduino mockedArduino = new Arduino(socat.getPort1());
                mockedArduino.addListener(new SerialMessageListener() {
                    @Override
                    public void onMessage(String msg) {
                        realArduino.println(msg);
                    }

                    @Override
                    public void onConnectionError(Exception e) {
                        e.printStackTrace();
                    }
                });
                realArduino.addListener(new SerialMessageListener() {
                    @Override
                    public void onMessage(String msg) {
                        mockedArduino.println(msg);
                    }

                    @Override
                    public void onConnectionError(Exception e) {
                        e.printStackTrace();
                    }
                });

                System.out.println("Loading focuser driver (MoonLite)...");
                server.loadNative("indi_moonlite_focus");

                System.out.println("Everything OK!");
                if (JOptionPane.showConfirmDialog(null, "Server started on port " + settings.getIndiPort() +
                                ", please connect the focuser to port " + socat.getPort2() + ". Copy to clipboard?",
                        APP_NAME, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    StringSelection toClipboard = new StringSelection(socat.getPort2());
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(toClipboard, toClipboard);
                }

                System.out.println("Starting system tray...");
                if (!SystemTray.isSupported()) {
                    System.err.println("System tray not supported!");
                    exit(ExitCodes.SYS_TRAY);
                }
                final PopupMenu popup = new PopupMenu();
                final TrayIcon trayIcon = new TrayIcon(logo);
                trayIcon.setImageAutoSize(true);
                trayIcon.setToolTip(APP_NAME);
                final SystemTray tray = SystemTray.getSystemTray();
                try {
                    tray.add(trayIcon);

                } catch (AWTException e) {
                    System.out.println("Tray icon could not be added!");
                    exit(13);
                }
                disposeTray = () -> tray.remove(trayIcon);
                MenuItem copyItem = new MenuItem("Copy focuser port");
                copyItem.addActionListener(e -> {
                    StringSelection toClipboard = new StringSelection(socat.getPort2());
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(toClipboard, toClipboard);
                });
                MenuItem aboutItem = new MenuItem("About");
                aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(null,
                        "Control an Arduino focuser and digital pins with " + APP_NAME + ", an INDI driver by SquareBoot",
                        APP_NAME, JOptionPane.INFORMATION_MESSAGE));
                MenuItem exitItem = new MenuItem("Exit");
                exitItem.addActionListener(e -> {
                    if (JOptionPane.showConfirmDialog(null, "Are you sure?",
                            APP_NAME, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        exit();
                    }
                });
                trayIcon.setPopupMenu(popup);
                trayIcon.displayMessage(APP_NAME,
                        "INDI server started successfully!", TrayIcon.MessageType.INFO);
            }

            @Override
            protected void onCancel() {
                exit();
            }
        };
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
        if (disposeTray != null) {
            disposeTray.run();
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
        SOCAT_ERROR(9),
        SYS_TRAY(10);

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