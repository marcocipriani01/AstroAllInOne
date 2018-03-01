package squareboot.astro.allinone;

import laazotea.indi.Constants;
import laazotea.indi.Constants.PropertyPermissions;
import laazotea.indi.Constants.PropertyStates;
import laazotea.indi.INDIException;
import laazotea.indi.driver.*;
import squareboot.astro.allinone.io.Arduino;
import squareboot.astro.allinone.io.ConnectionError;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;

/**
 * INDI Arduino pin generic driver.
 *
 * @author SquareBoot
 * @version 0.1
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class INDIPinDriver extends INDIDriver implements INDIConnectionHandler {

    /**
     * The board to control.
     */
    private final Arduino arduino;
    /**
     * The map of pins.
     */
    HashMap<Integer, Integer> pinsMap = new HashMap<>();
    // The properties and elements of this driver
    private INDISwitchProperty digPinProps;
    private INDINumberProperty pwmPinsProp;

    /**
     * Class constructor.
     *
     * @param arduino    an Arduino board. Must be already connected.
     * @param switchPins a list of digital pins (on/off only).
     * @param pwmPins    a list of PWM-capable pins.
     */
    public INDIPinDriver(Arduino arduino, int[] switchPins, int[] pwmPins) {
        super(System.in, System.out);
        this.arduino = arduino;
        // Restart the board to ensure that all the pins are turned off.
        arduino.println("RS");

        // Look for duplicated pins
        LinkedHashSet<Integer> checker = new LinkedHashSet<>();
        for (int p : switchPins) {
            checker.add(p);
        }
        for (int p : pwmPins) {
            checker.add(p);
        }
        if (checker.size() != (switchPins.length + pwmPins.length)) {
            throw new IllegalStateException("Duplicated pins found in the list!");
        }

        digPinProps = new INDISwitchProperty(this, "Digital pins", "Digital pins", "Control",
                PropertyStates.OK, PropertyPermissions.RW, Constants.SwitchRules.ANY_OF_MANY);
        for (int p : pwmPins) {
            String name = "Pin " + p;
            INDISwitchElement element = new INDISwitchElement(digPinProps, name, name, Constants.SwitchStatus.OFF);
            pinsMap.put(p, 0);
        }

        pwmPinsProp = new INDINumberProperty(this, "PWM pins", "PWM pins", "Control",
                PropertyStates.OK, PropertyPermissions.RW);
        for (int p : pwmPins) {
            String name = "PWM pin " + p;
            INDINumberElement element = new INDINumberElement(pwmPinsProp, name, name,
                    0, 0, 255, 1, "%f");
            pinsMap.put(p, 0);
        }
    }

    /**
     * Sets a pin's value.
     *
     * @param pin   the pin ID.
     * @param value the new value. Can be 0 to turn off, or 1 (for on/off digital pins), or 2→255 (for PWM-capable pins).
     */
    public void setPinValue(int pin, int value) {
        if (!arduino.isConnected()) {
            throw new ConnectionError(ConnectionError.Type.NOT_CONNECTED);
        }
        arduino.println(":AV" + String.format("%02d", pin) + String.format("%03d", value) + "#");
        pinsMap.replace(pin, value);
    }

    /**
     * Returns the name of the Driver
     */
    @Override
    public String getName() {
        return "INDI Arduino pin generic driver";
    }

    @Override
    public void processNewNumberValue(INDINumberProperty property, Date timestamp, INDINumberElementAndValue[] elementsAndValues) {
        if (property == pwmPinsProp) {
            for (INDINumberElementAndValue eAV : elementsAndValues) {
                INDINumberElement element = eAV.getElement();
                int pin = Integer.valueOf(element.getName().replace("PWM pin ", ""));
                int newValue = eAV.getValue().intValue();
                // Do not confuse value 1 (digital on) with a valid PWM value! See setPinValue(int, int) for further information!
                newValue = (newValue == 1) ? 2 : newValue;
                if (newValue != pinsMap.get(pin)) {
                    setPinValue(pin, newValue);
                    element.setValue((double) newValue);
                }
            }
            pwmPinsProp.setState(PropertyStates.OK);
            try {
                updateProperty(pwmPinsProp);

            } catch (INDIException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void processNewBLOBValue(INDIBLOBProperty property, Date timestamp, INDIBLOBElementAndValue[] elementsAndValues) {

    }

    @Override
    public void processNewTextValue(INDITextProperty property, Date timestamp, INDITextElementAndValue[] elementsAndValues) {

    }

    @Override
    public void processNewSwitchValue(INDISwitchProperty property, Date timestamp, INDISwitchElementAndValue[] elementsAndValues) {
        if (property == digPinProps) {
            for (INDISwitchElementAndValue eAV : elementsAndValues) {
                INDISwitchElement element = eAV.getElement();
                int pin = Integer.valueOf(element.getName().replace("Pin ", ""));
                Constants.SwitchStatus newValue = eAV.getValue();
                int newIntValue = newValue == Constants.SwitchStatus.ON ? 1 : 0;
                if (newIntValue != pinsMap.get(pin)) {
                    setPinValue(pin, newIntValue);
                    element.setValue(newValue);
                }
            }
            digPinProps.setState(PropertyStates.OK);
            try {
                updateProperty(digPinProps);

            } catch (INDIException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void driverConnect(Date timestamp) {
        printMessage("Driver connected");
        addProperty(digPinProps);
        addProperty(pwmPinsProp);
    }

    /**
     * Remove the image and send properties changes
     */
    @Override
    public void driverDisconnect(Date timestamp) {
        printMessage("Driver disconnected");
        removeProperty(digPinProps);
        removeProperty(pwmPinsProp);
    }
}