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
    private int pin = -1;
    @SerializedName("Name")
    @Expose
    private String name = "A pin";
    @SerializedName("Value")
    @Expose
    private int value = 0;

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
        this.value = constrainValue(value);
    }

    @SuppressWarnings("SameParameterValue")
    public static int percentageToPwm(int percentage) {
        return constrainValue((int) Math.round(percentage * 2.55));
    }

    @SuppressWarnings("SameParameterValue")
    public static int constrainValue(int n) {
        return (n >= 255 ? 255 : (n <= 0 ? 0 : n));
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
        this.value = constrainValue(value);
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
        return "Pin " + pin + " is \"" + name + "\", value: " +
                (value == 255 ? "high" : (value == 0 ? "low" : ((int) Math.round(value / 2.55) + "%")));
    }
}