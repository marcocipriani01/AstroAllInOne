package squareboot.astro.allinone;

import laazotea.indi.Constants.PropertyPermissions;
import laazotea.indi.Constants.PropertyStates;
import laazotea.indi.Constants.SwitchRules;
import laazotea.indi.Constants.SwitchStatus;
import laazotea.indi.INDIBLOBValue;
import laazotea.indi.driver.*;

import java.io.*;
import java.util.Date;

/**
 * @author SquareBoot
 * @version 0.1
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class INDIPWMDriver extends INDIDriver implements INDIConnectionHandler {

    // The properties and elements of this driver
    private INDIBLOBProperty imageP;
    private INDIBLOBElement imageE;
    private INDISwitchProperty sendP;
    private INDISwitchElement sendE;
    private INDITextProperty textText;
    private INDITextProperty text2Text;
    private INDITextElement textElement;
    private INDITextElement text2Element;

    /**
     * Class constructor.
     */
    public INDIPWMDriver(InputStream inputStream, OutputStream outputStream) {
        super(inputStream, outputStream);

        textText = new INDITextProperty(this, "Hey", "How are U?", "Peppe",
                PropertyStates.OK, PropertyPermissions.RO);
        text2Element = new INDITextElement(textText, "Hey", "Element", "Ahaha");

        text2Text = new INDITextProperty(this, "Bye", "How are U?", "Peppe",
                PropertyStates.OK, PropertyPermissions.RW);
        text2Element = new INDITextElement(text2Text, "Bye", "Element", "Ahahahahahahahahh");

        // Define the BLOB Property with this Driver as its owner, name "image", label "Image", group
        // "Image Properties", initial state IDLE and Read Only.
        imageP = new INDIBLOBProperty(this, "image", "Image",
                "Image Properties", PropertyStates.IDLE, PropertyPermissions.RO);

        // Define the BLOB Element with name "image" and label "Image". Its initial value is empty.
        imageE = new INDIBLOBElement(imageP, "image", "Image");

        // Define the Switch Property with this driver as its owner, name "sendImage", label "Send Image",
        // group "Image Properties", initial state IDLE, Read/Write permission and AtMostOne rule for the switch.
        sendP = new INDISwitchProperty(this, "sendImage", "Send Image",
                "Image Properties", PropertyStates.IDLE, PropertyPermissions.RW, SwitchRules.AT_MOST_ONE);

        // Define the Switch Element with name "sendImage", label "Send Image" and initial status OFF
        sendE = new INDISwitchElement(sendP, "sendImage", "Send Image", SwitchStatus.OFF);
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

    }

    @Override
    public void processNewBLOBValue(INDIBLOBProperty property, Date timestamp, INDIBLOBElementAndValue[] elementsAndValues) {

    }

    @Override
    public void processNewTextValue(INDITextProperty property, Date timestamp, INDITextElementAndValue[] elementsAndValues) {

    }

    @Override
    public void processNewSwitchValue(INDISwitchProperty property, Date timestamp, INDISwitchElementAndValue[] elementsAndValues) {
        try {
            // If the property is the sendImage one
            if (property == sendP) {
                // If any element has been updated
                if (elementsAndValues.length > 0) {
                    INDISwitchElement el = elementsAndValues[0].getElement();
                    SwitchStatus s = elementsAndValues[0].getValue();
                    // If the sendImage element has been switched one we send the image
                    if ((el == sendE) && (s == SwitchStatus.ON)) {
                        if (loadImageFromFile()) {
                            // Set the state of the sendImage property as OK
                            sendP.setState(PropertyStates.OK);
                            // Send the sendImage property to the client.
                            updateProperty(sendP);
                            // Set the state of the image property as OK
                            imageP.setState(PropertyStates.OK);
                            // Send the image property to the client.
                            updateProperty(imageP);
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void driverConnect(Date timestamp) {
        printMessage("Driver connect");
        addProperty(imageP);
        addProperty(sendP);
        addProperty(textText);
        addProperty(text2Text);
    }

    /**
     * Remove the image and send properties changes
     */
    @Override
    public void driverDisconnect(Date timestamp) {
        printMessage("Driver disconnect");
        removeProperty(imageP);
        removeProperty(sendP);
        removeProperty(textText);
        removeProperty(text2Text);
    }

    /**
     * Loads the image "image.jpg" from the same directory into the image property.
     *
     * @return {@code false} if the loading has been successful. {@code false} if otherwise.
     */
    private boolean loadImageFromFile() {
        // If it has not been already loaded
        if (imageE.getValue().getSize() == 0) {
            byte[] fileContents;

            try {
                InputStream in  = INDIPWMDriver.class.getResourceAsStream("/squareboot/astro/allinone/res/image.jpg");
                fileContents = new byte[in.available()];
                in.read(fileContents);
                in.close();

            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            // Create the new BLOB value and set it to the image element.
            INDIBLOBValue v = new INDIBLOBValue(fileContents, ".jpg");
            imageE.setValue(v);
        }
        return true;
    }
}