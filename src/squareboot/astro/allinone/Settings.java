package squareboot.astro.allinone;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import squareboot.astro.allinone.indi.DriverDefinition;
import squareboot.astro.allinone.indi.NativeDriverDefinition;
import squareboot.astro.allinone.serial.ArduinoPin;

import java.io.*;
import java.lang.reflect.ParameterizedType;
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

    public static Gson serializer;

    static {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        builder.registerTypeAdapter(new TypeToken<ArrayList<DriverDefinition>>(){}.getType(), new DriversListInstanceCreator());
        builder.registerTypeAdapter(new TypeToken<DriverDefinition>(){}.getType(), new DriverDefinitionInstanceCreator());
        builder.serializeNulls();
        serializer = builder.create();
    }

    public static class DriverDefinitionInstanceCreator implements InstanceCreator<DriverDefinition> {
        @SuppressWarnings("unchecked")
        public DriverDefinition createInstance(Type type) {
            Type[] typeParameters = ((ParameterizedType)type).getActualTypeArguments();
            /*if (typeParameters.length != 1) {
                System.err.println("Unable to deserialize a driver definition!");
                return null;
            }*/
            Type idType = typeParameters[0];
            System.out.println(idType.getTypeName());
            return new NativeDriverDefinition();
        }
    }

    public static class DriversListInstanceCreator implements InstanceCreator<ArrayList<DriverDefinition>> {
        @SuppressWarnings("unchecked")
        public ArrayList<DriverDefinition> createInstance(Type type) {
            return new ArrayList<>();
        }
    }

    @SerializedName("USB port")
    public String usbPort;
    @SerializedName("INDI server port")
    public int indiPort;
    @SerializedName("Custom drivers")
    public ArrayList<DriverDefinition> drivers; //TODO abstract -> gson
    @SerializedName("Digital pins")
    public ArrayList<ArduinoPin> digitalPins;
    @SerializedName("PWM pins")
    public ArrayList<ArduinoPin> pwmPins;
    @SerializedName("Nikon shutter pin")
    public int shutterCablePin;

    /**
     * Class constructor.
     */
    public Settings() {

    }

    /**
     * Loads the app's settings.
     *
     * @param file the input data file.
     */
    public static Settings load(File file) {
        StringBuilder json = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }

        } catch (IOException e) {
            return empty();
        }
        return serializer.fromJson(json.toString(), Settings.class);
    }

    /**
     * @return a new, empty, instance of this class.
     */
    public static Settings empty() {
        Settings s = new Settings();
        s.usbPort = "";
        s.indiPort = 7624;
        s.drivers = new ArrayList<>();
        s.digitalPins = new ArrayList<>();
        s.pwmPins = new ArrayList<>();
        s.shutterCablePin = 0;
        return s;
    }

    /**
     * Loads the app's settings.
     *
     * @param file the input data file.
     */
    public void save(File file) {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
            out.write(serializer.toJson(this));

        } catch (IOException e) {
            throw new UncheckedIOException("Unable to write in the output file!", e);
        }
    }
}