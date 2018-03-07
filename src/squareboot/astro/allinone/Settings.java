package squareboot.astro.allinone;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.*;
import java.util.ArrayList;

/**
 * Stores all the app's settings.
 *
 * @author SquareBoot
 * @version 0.1
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Settings {

    public static Gson serializer = new GsonBuilder()
            .setPrettyPrinting().serializeNulls().excludeFieldsWithoutExposeAnnotation().create();

    @SerializedName("USB port")
    @Expose
    public String usbPort = "";
    @SerializedName("INDI server port")
    @Expose
    public int indiPort = 7624;
    @SerializedName("Digital pins")
    @Expose
    public ArrayList<ArduinoPin> digitalPins = new ArrayList<>();
    @SerializedName("PWM pins")
    @Expose
    public ArrayList<ArduinoPin> pwmPins = new ArrayList<>();
    @Expose
    @SerializedName("Nikon shutter pin")
    public int shutterCablePin = -1;
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
        if (file.isDirectory()) {
            String path = file.getAbsolutePath();
            this.file = new File(path + (path.endsWith(File.separator) ? "" : File.separator) + "Settings.json");

        } else {
            this.file = file;
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
}