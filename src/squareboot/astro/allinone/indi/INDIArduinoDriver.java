package squareboot.astro.allinone.indi;

import laazotea.indi.Constants;
import laazotea.indi.Constants.PropertyPermissions;
import laazotea.indi.Constants.PropertyStates;
import laazotea.indi.INDIException;
import laazotea.indi.driver.*;
import squareboot.astro.allinone.io.Arduino;
import squareboot.astro.allinone.ArduinoPin;
import squareboot.astro.allinone.io.ConnectionError;

import java.io.InputStream;
import java.io.OutputStream;
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
public class INDIArduinoDriver extends INDIDriver implements INDIConnectionHandler {

    private static INDIArduinoDriver instance;
    /**
     * The map of pins.
     */
    HashMap<INDIElement, ArduinoPin> pinsMap = new HashMap<>();
    /**
     * The board to control.
     */
    private Arduino arduino;
    // The properties and elements of this driver
    private INDISwitchProperty digitalPinProps;
    private INDINumberProperty pwmPinsProp;

    /**
     * Class constructor.
     */
    public INDIArduinoDriver(InputStream inputStream, OutputStream outputStream) {
        super(inputStream, outputStream);
        instance = this;
    }

    /**
     * Singleton instance.
     */
    public static INDIArduinoDriver getInstance() {
        return instance;
    }

    /**
     * @param arduino    an Arduino board. Must be already connected.
     * @param switchPins a list of digital pins (on/off only).
     * @param pwmPins    a list of PWM-capable pins.
     */
    public void init(Arduino arduino, ArduinoPin[] switchPins, ArduinoPin[] pwmPins) {
        this.arduino = arduino;
        // Restart the board to ensure that all the pins are turned off.
        //arduino.println(":RS#"); //TODO

        // Look for duplicated pins
        LinkedHashSet<Integer> checker = new LinkedHashSet<>();
        for (ArduinoPin p : switchPins) {
            checker.add(p.getPin());
        }
        for (ArduinoPin p : pwmPins) {
            checker.add(p.getPin());
        }
        if (checker.size() != (switchPins.length + pwmPins.length)) {
            throw new IllegalStateException("Duplicated pins found in the list!");
        }

        digitalPinProps = new INDISwitchProperty(this, "Digital pins", "Digital pins", "Control",
                PropertyStates.OK, PropertyPermissions.RW, Constants.SwitchRules.ANY_OF_MANY);
        for (ArduinoPin pin : pwmPins) {
            pinsMap.put(new INDISwitchElement(digitalPinProps, "Pin " + pin.getPin(),
                    pin.getName(), Constants.SwitchStatus.OFF), pin);
        }

        pwmPinsProp = new INDINumberProperty(this, "PWM pins", "PWM pins", "Control",
                PropertyStates.OK, PropertyPermissions.RW);
        for (ArduinoPin pin : pwmPins) {
            pinsMap.put(new INDINumberElement(pwmPinsProp, "PWM pin" + pin.getPin(), pin.getName(),
                    0, 0, 255, 1, "%f"), pin);
        }
    }

    /**
     * Updates a pin's value.
     *
     * @param pin the pin ID.
     */
    public void updatePin(ArduinoPin pin) {
        if (!arduino.isConnected()) {
            throw new ConnectionError(ConnectionError.Type.NOT_CONNECTED);
        }
        arduino.println(":AV" + String.format("%02d", pin.getPin()) + String.format("%03d", pin.getValue()) + "#");
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
                ArduinoPin pin = pinsMap.get(element);
                int newValue = eAV.getValue().intValue();
                if (newValue != pin.getValue()) {
                    pin.setValue(newValue);
                    updatePin(pin);
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
        if (property == digitalPinProps) {
            for (INDISwitchElementAndValue eAV : elementsAndValues) {
                INDISwitchElement element = eAV.getElement();
                ArduinoPin pin = pinsMap.get(element);
                int newValue = eAV.getValue() == Constants.SwitchStatus.ON ? 255 : 0;
                if (newValue != pin.getValue()) {
                    pin.setValue(newValue);
                    updatePin(pin);
                }
            }
            digitalPinProps.setState(PropertyStates.OK);
            try {
                updateProperty(digitalPinProps);

            } catch (INDIException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void driverConnect(Date timestamp) {
        addProperty(digitalPinProps);
        addProperty(pwmPinsProp);
    }

    /**
     * Remove the image and send properties changes
     */
    @Override
    public void driverDisconnect(Date timestamp) {
        removeProperty(digitalPinProps);
        removeProperty(pwmPinsProp);
    }
}