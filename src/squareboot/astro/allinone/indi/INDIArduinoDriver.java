package squareboot.astro.allinone.indi;

import laazotea.indi.Constants;
import laazotea.indi.Constants.PropertyPermissions;
import laazotea.indi.Constants.PropertyStates;
import laazotea.indi.INDIException;
import laazotea.indi.driver.*;
import squareboot.astro.allinone.ArduinoPin;
import squareboot.astro.allinone.PinValue;
import squareboot.astro.allinone.io.ConnectionError;
import squareboot.astro.allinone.io.GenericSerialPort;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
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

    /**
     * An instance of this class.
     */
    private static INDIArduinoDriver instance;
    /**
     * The map of pins.
     */
    HashMap<INDIElement, ArduinoPin> pinsMap = new HashMap<>();
    /**
     * The board to control.
     */
    private GenericSerialPort serialPort;
    // The properties and elements of this driver
    /**
     * All the properties used by digital pins.
     */
    private INDISwitchProperty digitalPinProps;
    /**
     * All the properties used by PWM pins.
     */
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
     * @param serialPort an Arduino board. Must be already connected.
     * @param switchPins a list of digital pins (on/off only).
     * @param pwmPins    a list of PWM-capable pins.
     */
    public void init(GenericSerialPort serialPort, ArduinoPin[] switchPins, ArduinoPin[] pwmPins) {
        this.serialPort = serialPort;
        // Restart the board to ensure that all the pins are turned off.
        serialPort.print(":RS#");

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
        for (ArduinoPin pin : switchPins) {
            printMessage("onDigitalPinCreate(" + pin + ")");
            pinsMap.put(new INDISwitchElement(digitalPinProps, "Pin " + pin.getPin(),
                    pin.getName(), pin.getValueIndi()), pin);
        }

        pwmPinsProp = new INDINumberProperty(this, "PWM pins", "PWM pins", "Control",
                PropertyStates.OK, PropertyPermissions.RW);
        for (ArduinoPin pin : pwmPins) {
            printMessage("onPwmPinCreate(" + pin + ")");
            pinsMap.put(new INDINumberElement(pwmPinsProp, "PWM pin" + pin.getPin(), pin.getName(),
                    (double) pin.getValuePercentage(), 0.0, 100.0, 1.0, "%f"), pin);
        }
    }

    /**
     * Forces the board to restart. Also cleans all the pins' values.
     */
    public void forceReboot() {
        if (!serialPort.isConnected()) {
            throw new ConnectionError(ConnectionError.Type.NOT_CONNECTED);
        }
        printMessage("Force reboot invoked!");
        serialPort.print(":RS#");
        printMessage("Cleaning all the values of the map...");
        for (INDIElement element : pinsMap.keySet()) {
            if (element instanceof INDINumberElement) {
                element.setValue(0.0);

            } else if (element instanceof INDISwitchElement) {
                element.setValue(Constants.SwitchStatus.OFF);
            }
        }
        for (ArduinoPin pin : pinsMap.values()) {
            pin.setPinVal(new PinValue());
        }
        try {
            ArrayList<INDIProperty> properties = getPropertiesAsList();
            // Avoid updating properties that haven't been added (if this driver hasn't been connected yet to a server)
            if (properties.contains(digitalPinProps)) {
                updateProperty(digitalPinProps);
            }
            if (properties.contains(pwmPinsProp)) {
                updateProperty(pwmPinsProp);
            }

        } catch (INDIException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates a pin's value.
     *
     * @param pin the pin ID.
     */
    public void updatePin(ArduinoPin pin) {
        serialPort.print(":AV" + String.format("%02d", pin.getPin()) + String.format("%03d", pin.getValuePwm()) + "#");
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
                PinValue newValue = new PinValue(PinValue.ValueType.PERCENTAGE, eAV.getValue().intValue());
                if (!newValue.equals(pin.getPinVal())) {
                    pin.setPinVal(newValue);
                    element.setValue((double) pin.getValuePercentage());
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
                PinValue newValue = new PinValue(PinValue.ValueType.INDI, eAV.getValue());
                if (!newValue.equals(pin.getPinVal())) {
                    pin.setPinVal(newValue);
                    element.setValue(newValue.getValueIndi());
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
        printMessage("Driver connection");
        addProperty(digitalPinProps);
        addProperty(pwmPinsProp);
    }

    @Override
    public void driverDisconnect(Date timestamp) {
        printMessage("Driver disconnection");
        removeProperty(digitalPinProps);
        removeProperty(pwmPinsProp);
    }
}