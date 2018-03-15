package squareboot.astro.allinone;

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

    /**
     * Class constructor.
     */
    public ArduinoPin(int pin, String name) {
        this.pin = pin;
        this.name = name;
        this.value = 0;
    }

    /**
     * @return the stored value of the pin.
     */
    public int getValue() {
        return value;
    }

    /**
     * @param value the new value.
     */
    public void setValue(int value) {
        this.value = value;
    }

    /**
     * @return the pin.
     */
    public int getPin() {
        return pin;
    }

    /**
     * @param pin a new pin.
     */
    public void setPin(int pin) {
        this.pin = pin;
    }

    /**
     * @return the name of the pin.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name a new name for this pin.
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Pin " + name + " @" + pin;
    }
}