package squareboot.astro.allinone;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import laazotea.indi.driver.INDIDriver;
import squareboot.astro.allinone.indi.DriverDefinition;
import squareboot.astro.allinone.indi.DriverNotFoundException;
import squareboot.astro.allinone.indi.JavaDriverDefinition;
import squareboot.astro.allinone.indi.NativeDriverDefinition;
import squareboot.astro.allinone.serial.ArduinoPin;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Stores all the app's settings.
 *
 * @author SquareBoot
 * @version 0.1
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Settings {

    private static final String UNSUPPORTED = "unsupported";
    public static Gson serializer;

    static {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        builder.registerTypeAdapter(new TypeToken<ArrayList<DriverDefinition>>() {
        }.getType(), new DriversListInstanceCreator());
        builder.registerTypeAdapter(DriverDefinition.class, new DriverDefinitionSerializer());
        builder.registerTypeAdapter(DriverDefinition.class, new DriverDefinitionDeserializer());
        builder.serializeNulls();
        builder.excludeFieldsWithoutExposeAnnotation();
        serializer = builder.create();
    }

    @SerializedName("USB port")
    @Expose
    public String usbPort = "";
    @SerializedName("INDI server port")
    @Expose
    public int indiPort = 7624;
    @SerializedName("Custom drivers")
    @Expose
    public ArrayList<DriverDefinition> drivers = new ArrayList<>();
    @SerializedName("List of installed drivers")
    @Expose
    public ArrayList<DriverDefinition> installedNativeDrivers = new ArrayList<>();
    @SerializedName("Digital pins")
    @Expose
    public ArrayList<ArduinoPin> digitalPins = new ArrayList<>();
    @SerializedName("PWM pins")
    @Expose
    public ArrayList<ArduinoPin> pwmPins = new ArrayList<>();
    @Expose
    @SerializedName("Nikon shutter pin")
    public int shutterCablePin = -1;
    @Expose
    @SerializedName("Drivers location")
    public String indiDriversLocation = "/usr/bin";
    private File file;

    /**
     * Class constructor.
     */
    public Settings() {

    }

    /**
     * Class constructor.
     *
     * @param file a file where to save the settings.
     */
    public Settings(File file) {
        setFile(file);
    }

    /**
     * Loads the app's settings.
     *
     * @param file the input data file.
     */
    public static Settings load(File file) {
        if (file.isDirectory()) {
            String path = file.getAbsolutePath();
            file = new File(path + (path.endsWith(File.separator) ? "" : File.separator) + "Settings.json");
        }
        StringBuilder json = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }

        } catch (IOException e) {
            return new Settings(file);
        }
        Settings s = serializer.fromJson(json.toString(), Settings.class);
        s.setFile(file);
        return s;
    }

    /**
     * @return the current output file.
     */
    public File getFile() {
        return file;
    }

    /**
     * @param file an output file for the settings.
     */
    public void setFile(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                String path = file.getAbsolutePath();
                this.file = new File(path + (path.endsWith(File.separator) ? "" : File.separator) + "Settings.json");

            } else if (file.isFile()) {
                this.file = file;

            } else {
                throw new IllegalArgumentException("Unable to fetch the settings file!");
            }

        } else {
            if (file.getParentFile().mkdirs()) {
                setFile(file);

            } else {
                throw new IllegalStateException("Unable to create the settings file!");
            }
        }
    }

    /**
     * Saves the app's settings.
     *
     * @see #setFile(File)
     * @see #getFile()
     */
    public void save() {
        if (file == null) {
            throw new IllegalStateException("Output file not set!");
        }
        if (file.isDirectory()) {
            String path = file.getAbsolutePath();
            file = new File(path + (path.endsWith(File.separator) ? "" : File.separator) + "Settings.json");
        }
        try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
            out.write(serializer.toJson(this));

        } catch (IOException e) {
            throw new UncheckedIOException("Unable to write in the output file!", e);
        }
    }

    /**
     * {@link ArrayList<DriverDefinition>} instance creator for Gson.
     *
     * @author SquareBoot
     * @version 0.1
     */
    public static class DriversListInstanceCreator implements InstanceCreator<ArrayList<DriverDefinition>> {

        @Override
        public ArrayList<DriverDefinition> createInstance(Type type) {
            return new ArrayList<>();
        }
    }

    /**
     * {@link DriverDefinition} deserializer for Gson.
     *
     * @author SquareBoot
     * @version 0.1
     */
    private static class DriverDefinitionSerializer implements JsonSerializer<DriverDefinition> {

        @Override
        public JsonElement serialize(DriverDefinition src, Type typeOfSrc, JsonSerializationContext context) {
            if (src instanceof JavaDriverDefinition) {
                return new JsonPrimitive(((JavaDriverDefinition) src).getDriverClass().getName() + '&' + src.getIdentifier());

            } else if (src instanceof NativeDriverDefinition) {
                return new JsonPrimitive(((NativeDriverDefinition) src).getPath().getAbsolutePath() + '&' + src.getIdentifier());

            } else {
                new DriverNotFoundException("Unknown driver family!").printStackTrace();
                return new JsonPrimitive(UNSUPPORTED);
            }
        }
    }

    /**
     * {@link DriverDefinition} deserializer for Gson.
     *
     * @author SquareBoot
     * @version 0.1
     */
    private static class DriverDefinitionDeserializer implements JsonDeserializer<DriverDefinition> {

        @Override
        @SuppressWarnings("unchecked")
        public DriverDefinition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            String s = json.getAsJsonPrimitive().getAsString();
            if (s.equals(UNSUPPORTED)) {
                new DriverNotFoundException("Unknown driver family!").printStackTrace();
                return null;
            }
            String[] split = s.split("&");
            if (split.length != 2) {
                System.err.println("Error: could not restore invalid driver definition!");
                return null;
            }
            if (s.contains(".")) {
                try {
                    return new JavaDriverDefinition((Class<? extends INDIDriver>) Class.forName(split[0]), split[1]);

                } catch (ClassNotFoundException e) {
                    new DriverNotFoundException("Java driver class not found!").printStackTrace();
                    return null;
                }

            } else if (s.contains(File.separator)) {
                File f = new File(split[0]);
                if (f.exists() && f.isFile()) {
                    return new NativeDriverDefinition(f, split[1]);

                } else {
                    new DriverNotFoundException("Native driver executable not found!").printStackTrace();
                    return null;
                }

            } else {
                new DriverNotFoundException("Unknown driver family!").printStackTrace();
                return null;
            }
        }
    }
}