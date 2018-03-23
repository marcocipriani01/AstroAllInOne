package squareboot.astro.allinone;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import laazotea.indi.Constants;

import java.util.Objects;

/**
 * @author SquareBoot
 * @version 0.1
 */
@SuppressWarnings({"unused", "WeakerAccess", "SameParameterValue"})
public class PinValue {

    /**
     * The value (PWM).
     */
    @Expose
    @SerializedName("PWM")
    private int value = 0;

    /**
     * Class constructor.
     */
    public PinValue() {

    }

    /**
     * Class constructor.
     */
    public PinValue(int value) {
        setValue(value);
    }

    /**
     * Class constructor.
     */
    public PinValue(ValueType type, Object value) {
        setValue(type, value);
    }

    public static int percentageToPwm(int percentage) {
        return constrain((int) Math.round(percentage * 2.55), 0, 255);
    }

    public static int pwmToPercentage(int pwm) {
        return constrain((int) Math.round(pwm / 2.55), 0, 100);
    }

    public static int constrain(int n, int min, int max) {
        return (n >= max ? max : (n <= min ? min : n));
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean equals(PinValue obj) {
        return obj.getValuePwm() == value;
    }

    public int getValuePwm() {
        return value;
    }

    public boolean getValueBoolean() {
        return value >= 255;
    }

    public int getValuePercentage() {
        return pwmToPercentage(value);
    }

    public Constants.SwitchStatus getValueIndi() {
        return value == 255 ? Constants.SwitchStatus.ON : Constants.SwitchStatus.OFF;
    }

    public void setValue(ValueType type, Object value) {
        Objects.requireNonNull(value, "Null value!");
        switch (Objects.requireNonNull(type)) {
            case PERCENTAGE: {
                if (!(value instanceof Integer)) {
                    throw new IllegalArgumentException("Invalid value!");
                }
                this.value = percentageToPwm((int) value);
                break;
            }

            case PWM: {
                if (!(value instanceof Integer)) {
                    throw new IllegalArgumentException("Invalid value!");
                }
                this.value = constrain((int) value, 0, 255);
                break;
            }

            case INDI: {
                if (!(value instanceof Constants.SwitchStatus)) {
                    throw new IllegalArgumentException("Invalid value!");
                }
                this.value = value == Constants.SwitchStatus.ON ? 255 : 0;
                break;
            }

            case BOOLEAN: {
                if (!(value instanceof Boolean)) {
                    throw new IllegalArgumentException("Invalid value!");
                }
                this.value = ((boolean) value) ? 255 : 0;
                break;
            }

            default: {
                throw new UnsupportedOperationException("Unsupported type of pin value!");
            }
        }
    }

    public void setValue(int value) {
        this.value = constrain(value, 0, 255);
    }

    @Override
    public String toString() {
        return (value == 255 ? "high" : (value == 0 ? "low" : ((int) Math.round(value / 2.55) + "%")));
    }

    /**
     * @author SquareBoot
     * @version 0.1
     */
    public enum ValueType {
        PERCENTAGE, PWM, INDI, BOOLEAN
    }
}