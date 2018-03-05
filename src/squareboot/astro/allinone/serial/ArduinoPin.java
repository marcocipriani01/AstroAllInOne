package squareboot.astro.allinone.serial;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author SquareBoot
 * @version 0.1
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ArduinoPin {

    @Expose
    @SerializedName("Pin")
    private int pin;
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("Value")
    @Expose
    private int value;

    /**
     * Class constructor.
     */
    public ArduinoPin() {

    }

    /**
     * Class constructor.
     */
    public ArduinoPin(int pin, String name, int value) {
        this.pin = pin;
        this.name = name;
        this.value = value;
    }

    public int getPin() {
        return pin;
    }

    public void setPin(int pin) {
        this.pin = pin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}