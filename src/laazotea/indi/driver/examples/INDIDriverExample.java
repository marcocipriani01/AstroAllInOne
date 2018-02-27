/*
 *  This file is part of INDI for Java Driver.
 * 
 *  INDI for Java Driver is free software: you can redistribute it
 *  and/or modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation, either version 3 of 
 *  the License, or (at your option) any later version.
 * 
 *  INDI for Java Driver is distributed in the hope that it will be
 *  useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 *  of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with INDI for Java Driver.  If not, see 
 *  <http://www.gnu.org/licenses/>.
 */
package laazotea.indi.driver.examples;

import java.io.*;
import java.util.Date;
import laazotea.indi.Constants.PropertyPermissions;
import laazotea.indi.Constants.PropertyStates;
import laazotea.indi.Constants.SwitchRules;
import laazotea.indi.Constants.SwitchStatus;
import laazotea.indi.INDIBLOBValue;
import laazotea.indi.INDIException;
import laazotea.indi.driver.*;

/**
 * An example class representing a very basic INDI Driver.
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.3, April 5, 2012
 */
public class INDIDriverExample extends INDIDriver implements INDIConnectionHandler {

  // The Properties and Elements of this Driver
  private INDIBLOBProperty imageP;
  private INDIBLOBElement imageE;
  private INDISwitchProperty sendP;
  private INDISwitchElement sendE;

  public INDIDriverExample(InputStream inputStream, OutputStream outputStream) {
    super(inputStream, outputStream);

    // Define the BLOB Property with this Driver as its owner, name "image", label "Image", group "Image Properties", initial state IDLE and Read Only.
    imageP = new INDIBLOBProperty(this, "image", "Image", "Image Properties", PropertyStates.IDLE, PropertyPermissions.RO);
    // Define the BLOB Element with name "image" and label "Image". Its initial value is empty.
    imageE = new INDIBLOBElement(imageP, "image", "Image");


    // Define the Switch Property with this driver as its owner, name "sendImage", label "Send Image", group "Image Properties", initial state IDLE, Read/Write permission and AtMostOne rule for the switch.
    sendP = new INDISwitchProperty(this, "sendImage", "Send Image", "Image Properties", PropertyStates.IDLE, PropertyPermissions.RW, SwitchRules.AT_MOST_ONE);
    // Define the Switch Element with name "sendImage", label "Send Image" and initial status OFF
    sendE = new INDISwitchElement(sendP, "sendImage", "Send Image", SwitchStatus.OFF);
  }

  // Returns the name of the Driver
  @Override
  public String getName() {
    return "INDI Driver Example";
  }

  // No Number Properties can be set by clients. Empty method  
  @Override
  public void processNewNumberValue(INDINumberProperty property, Date timestamp, INDINumberElementAndValue[] elementsAndValues) {
  }

  // No BLOB Properties can be set by clients. Empty method  
  @Override
  public void processNewBLOBValue(INDIBLOBProperty property, Date timestamp, INDIBLOBElementAndValue[] elementsAndValues) {
  }

  // No Text Properties can be set by clients. Empty method
  @Override
  public void processNewTextValue(INDITextProperty property, Date timestamp, INDITextElementAndValue[] elementsAndValues) {
  }

  // Processes the changes sent by the client to the Switch Properties. If the Switch property is the CONNECTION one it adds or removes the image and send Properties. If the Property is the "sendImage", it loads an image from disk, sets it to the image property and sends it back to the client.
  @Override
  public void processNewSwitchValue(INDISwitchProperty property, Date timestamp, INDISwitchElementAndValue[] elementsAndValues) {

    if (property == sendP) {  // If the property is the sendImage one
      if (elementsAndValues.length > 0) {  // If any element has been updated
        INDISwitchElement el = elementsAndValues[0].getElement();
        SwitchStatus s = elementsAndValues[0].getValue();

        if ((el == sendE) && (s == SwitchStatus.ON)) {  // If the sendImage element has been switched one we send the image
          boolean imageLoaded = loadImageFromFile();

          if (imageLoaded) {
            sendP.setState(PropertyStates.OK);  // Set the state of the sendImage property as OK

            imageP.setState(PropertyStates.OK); // Set the state of the image property as OK

            try {
              updateProperty(sendP); // Send the sendImage property to the client.
              updateProperty(imageP); // Send the image property to the client.
            } catch (INDIException e) {
              e.printStackTrace();
              System.exit(-1);
            }
          }
        }
      }
    }
  }

  // Add the image and send properties changes
  @Override
  public void driverConnect(Date timestamp) {
    printMessage("Driver connect");
    this.addProperty(imageP);
    this.addProperty(sendP);
  }

  // Remove the image and send properties changes
  @Override
  public void driverDisconnect(Date timestamp) {
    printMessage("Driver disconnect");
    this.removeProperty(imageP);
    this.removeProperty(sendP);
  }

  // Loads the image "image.jpg" from the same directory into the image property. Returns true if the loading has been succesful. false otherwise.
  private boolean loadImageFromFile() {
    if (imageE.getValue().getSize() == 0) { // If it has not been already loaded

      byte[] fileContents;

      try {
        File file = new File("image.jpg");

// Create a buffer big enough to hold the file
        int size = (int) file.length();
        fileContents = new byte[size];
// Create an input stream from the file object and read it all
        FileInputStream in = new FileInputStream(file);
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
