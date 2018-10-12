package squareboot.astro.allinone;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;

/**
 * @author SquareBoot
 * @version 0.1
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class PinArray {

    @SerializedName("List")
    @Expose
    private ArrayList<ArduinoPin> list;

    /**
     * Class constructor.
     */
    public PinArray() {
        list = new ArrayList<>();
    }

    public int size() {
        return list.size();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public boolean contains(int pin) {
        for (ArduinoPin ap : list) {
            if (ap.getPin() == pin) {
                return true;
            }
        }
        return false;
    }

    public boolean contains(ArduinoPin pin) {
        if (pin == null) {
            throw new NullPointerException("Null pin!");
        }
        int param = pin.getPin();
        for (ArduinoPin ap : list) {
            if (ap.getPin() == param) {
                return true;
            }
        }
        return false;
    }

    public int indexOf(ArduinoPin pin) {
        if (pin == null) {
            throw new NullPointerException("Null pin!");
        }
        return list.indexOf(pin);
    }

    public ArduinoPin[] toArray() {
        Object[] array = list.toArray();
        return Arrays.copyOf(array, array.length, ArduinoPin[].class);
    }

    public void add(ArduinoPin pin) {
        if (pin == null) {
            throw new NullPointerException("Null pin!");
        }
        if (contains(pin)) {
            throw new IllegalStateException("Pin already in list!");
        }
        list.add(pin);
    }

    public void remove(ArduinoPin pin) {
        if (pin == null) {
            throw new NullPointerException("Null pin!");
        }
        list.remove(pin);
    }

    public void clear() {
        list.clear();
    }

    /**
     * @param pins lists of pins
     * @return {@code true} if no duplicates are found.
     * @throws IndexOutOfBoundsException if a pin is outside the allowed bounds (2 ≤ pin ≤ 99)
     * */
    public static boolean checkPins(ArduinoPin[]... pins) {
        LinkedHashSet<Integer> checker = new LinkedHashSet<>();
        int size = 0;
        for (ArduinoPin[] a : pins) {
            size += a.length;
            for (ArduinoPin p : a) {
                int n = p.getPin();
                if ((n < 2) || (n > 99)) {
                    throw new IndexOutOfBoundsException("Invalid pin: " + p + "\" is outside the allowed bounds (2 ≤ pin ≤ 99)!");
                }
                checker.add(n);
            }
        }
        return checker.size() == size;
    }
}