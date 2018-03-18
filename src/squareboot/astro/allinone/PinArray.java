package squareboot.astro.allinone;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;

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
        int param = pin.getPin();
        for (ArduinoPin ap : list) {
            if (ap.getPin() == param) {
                return true;
            }
        }
        return false;
    }

    public int indexOf(ArduinoPin o) {
        return list.indexOf(o);
    }

    public ArduinoPin[] toArray() {
        Object[] array = list.toArray();
        return Arrays.copyOf(array, array.length, ArduinoPin[].class);
    }

    public boolean add(ArduinoPin arduinoPin) {
        if (contains(arduinoPin)) {
            throw new IllegalStateException("Pin already in list!");
        }
        return list.add(arduinoPin);
    }

    public boolean remove(ArduinoPin o) {
        return list.remove(o);
    }

    public void clear() {
        list.clear();
    }
}