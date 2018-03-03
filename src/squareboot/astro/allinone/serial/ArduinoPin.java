package squareboot.astro.allinone.serial;

import com.google.gson.annotations.SerializedName;

/**
 * @author SquareBoot
 * @version 0.1
 */
public class ArduinoPin {

    @SerializedName("Pin")
    private int pin;
    @SerializedName("Name")
    private String name;
    @SerializedName("Value")
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