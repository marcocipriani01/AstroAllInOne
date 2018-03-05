package squareboot.astro.allinone;

import com.google.gson.reflect.TypeToken;
import org.apache.commons.cli.*;
import squareboot.astro.allinone.indi.*;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
     * Global settings
     */
    public static Settings settings;
    /**
     *
     * */
    public static DriverDefinition[] nativeDrivers;

    /**
     * Main. Configures the Look and Feel and starts the application.
     *
     * @see <a href="https://github.com/khuxtable/seaglass/releases/tag/seaglasslookandfeel-0.2.1">Seaglass L&F - GitHub</a>
     * @see <a href="https://mvnrepository.com/artifact/com.seaglasslookandfeel/seaglasslookandfeel/0.2.1">Seaglass L&f - Maven repository</a>
     * @see <a href="http://stackoverflow.com/questions/9123002/how-to-install-configure-custom-java-look-and-feel">How to install/configure custom Java Look-And-Feel?</a>
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
        options.addOption("i", "indi-drivers", true,
                "Specifies the directory where all the native INDI drivers are located. Default: /usr/bin");

        try {
            CommandLine line = parser.parse(options, args);

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

            if (line.hasOption('i')) {
                String location = line.getOptionValue('i');
                File f = new File(location);
                if (f.exists() && f.isDirectory()) {
                    settings.indiDriversLocation = location;

                } else {
                    System.err.println("Invalid directory for native INDI drivers!");
                    System.exit(7);
                }
            }

        } catch (ParseException e) {
            System.err.println("The given arguments are invalid!");
            System.exit(8);
        }

        try {
            System.out.println("Loading native drivers...");
            File[] list = new File(settings.indiDriversLocation).listFiles();
            if (list == null) {
                System.err.println("Invalid directory for native INDI drivers!");
                System.exit(7);
            }
            ArrayList<File> installedDriverDefinitions = new ArrayList<>();
            for (File f : list) {
                if (f.isFile() && f.getName().startsWith("indi_")) {
                    installedDriverDefinitions.add(f);
                }
            }

            ArrayList<DriverDefinition> storedDrivers = settings.installedNativeDrivers;
            ArrayList<NativeDriverDefinition> nativeStoredDrivers = new ArrayList<>();
            for (DriverDefinition sdd : storedDrivers) {
                if (sdd instanceof NativeDriverDefinition) {
                    nativeStoredDrivers.add((NativeDriverDefinition) sdd);

                } else {
                    nativeStoredDrivers.clear();
                    break;
                }
            }
            System.out.println("Starting server...");
            INDIServer server; //TODO
            server = new INDIServer(settings.indiPort);
            //TOREPLACE
            for (NativeDriverDefinition storedDefinition : nativeStoredDrivers) {
                for (File installedDefinition : installedDriverDefinitions) {
                    if (!storedDefinition.getPath().getAbsolutePath().equals(installedDefinition.getAbsolutePath())) {
                        nativeStoredDrivers.add(
                                new NativeDriverDefinition(
                                        installedDefinition,
                                        NativeDriverDefinition.processIdentifier(installedDefinition.getAbsolutePath(), server)));
                    }
                }
            }
            //NEW
            for (File installedDefinition : installedDriverDefinitions) {
                String path = installedDefinition.getAbsolutePath();
                boolean contains = false;
                for (NativeDriverDefinition storedDefinition : nativeStoredDrivers) {
                    if (storedDefinition.getPath().getAbsolutePath().equals(path)) {
                        contains = true;
                    }
                }
                if (contains) {

                }
                if (!storedDefinition.getPath().getAbsolutePath().equals()) {
                    nativeStoredDrivers.add(
                            new NativeDriverDefinition(
                                    installedDefinition,
                                    NativeDriverDefinition.processIdentifier(installedDefinition.getAbsolutePath(), server)));
                }
            }

            Object[] array = nativeStoredDrivers.toArray();
            nativeDrivers = Arrays.copyOf(array, array.length, DriverDefinition[].class);

        } catch (Exception e) {
            System.err.println("Could not load native drivers in /usr/bin!");
        }

        /*System.out.println("Starting server...");
        INDIServer server = new INDIServer();
        System.out.println("Loading drivers...");
        server.loadJava(INDIArduinoDriver.class);*/

        // find /usr/bin -name indi_* -perm /u+x -type f > "$(dirname "$0")/drivers"
        //if ()

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
            /*INDIArduinoDriver arduinoDriver = INDIArduinoDriver.getInstance();
        if (arduinoDriver == null) {
            System.out.println("Ahi");
            return;
        }
        //arduinoDriver.init(arduino, new ArduinoPin[]{new ArduinoPin(122, "c2", 122)}, new ArduinoPin[]{new ArduinoPin(12, "c", 12)});
        file = new File(args[0]);
        //settings = Settings.load(file);
        settings = Settings.empty();
        settings.drivers.add(new JavaDriverDefinition(arduinoDriver.getClass(), "peppe"));
        settings.save(file);
        settings = Settings.load(file);
        System.out.println(settings.drivers.get(0).toString());
        //new ControlPanel();*/
    }

    private static void loadNativeDriversDefinitions() {
        //TODO
    }

    /**
     * Loads a font from a package.
     *
     * @param pck the package where the font is located.
     * @return the required {@code Font} Object.
     */
    @SuppressWarnings("SameParameterValue")
    private static Font loadFont(String pck) throws IOException, FontFormatException {
        return Font.createFont(Font.TRUETYPE_FONT, Main.class.getResourceAsStream(pck));
    }
}