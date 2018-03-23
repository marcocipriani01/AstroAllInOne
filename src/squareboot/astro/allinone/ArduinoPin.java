package squareboot.astro.allinone;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import laazotea.indi.Constants;

import java.util.Objects;

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
    private PinValue value = new PinValue();

    /**
     * Class constructor. For Gson only!
     */
    public ArduinoPin() {

    }

    /**
     * Class constructor.
     */
    public ArduinoPin(int pin, String name) {
        this.pin = pin;
        if (name != null) {
            this.name = name;
        }
    }

    /**
     * Class constructor.
     */
    public ArduinoPin(int pin, String name, PinValue value) {
        this(pin, name);
        this.value = Objects.requireNonNull(value, "Null pin value!");
    }

    /**
     * Class constructor.
     */
    public ArduinoPin(int pin, String name, int value) {
        this(pin, name);
        this.value = new PinValue(value);
    }

    public int getValuePwm() {
        return value.getValuePwm();
    }

    public boolean getValueBoolean() {
        return value.getValueBoolean();
    }

    public int getValuePercentage() {
        return value.getValuePercentage();
    }

    public Constants.SwitchStatus getValueIndi() {
        return value.getValueIndi();
    }

    public void setValue(PinValue.ValueType type, Object value) {
        this.value.setValue(type, value);
    }

    public void setValue(int value) {
        this.value.setValue(value);
    }

    /**
     * @return the stored value of the pin.
     */
    public PinValue getPinVal() {
        return value;
    }

    /**
     * @param value the new value.
     */
    public void setPinVal(PinValue value) {
        this.value = Objects.requireNonNull(value);
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
        this.name = Objects.requireNonNull(name, "Null name!");
    }

    @Override
    public String toString() {
        return "Pin " + pin + " is \"" + name + "\", value: " + value.toString();
    }
}