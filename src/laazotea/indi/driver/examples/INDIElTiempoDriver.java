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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import laazotea.indi.Constants.PropertyPermissions;
import laazotea.indi.Constants.PropertyStates;
import laazotea.indi.Constants.SwitchRules;
import laazotea.indi.Constants.SwitchStatus;
import laazotea.indi.INDIBLOBValue;
import laazotea.indi.INDIException;
import laazotea.indi.driver.*;

/**
 * A small example Driver that uses the INDI for Java Driver library. It defines
 * two BLOB Properties, two Text Properties and a Switch One. The BLOB
 * Properties will have two images about the weather in Spain and Europe
 * (dinamically downloaded from http://eltiempo.es), and the Text ones will
 * contain the names of them. It will check for updated images every 15 minutes.
 * The Switch Property can be used to ask for the current images (for example
 * once the client connects to the driver).
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.3, April 5, 2012
 */
public class INDIElTiempoDriver extends INDIDriver implements Runnable, INDIConnectionHandler {

  /*
   * The properties
   */
  private INDISwitchElement sendImage;
  private INDISwitchProperty send;
  private INDIBLOBProperty spainImageProp;
  private INDIBLOBElement spainImageElem;
  private INDITextProperty spainImageNameProp;
  private INDITextElement spainImageNameElem;
  private INDIBLOBProperty europeImageProp;
  private INDIBLOBElement europeImageElem;
  private INDITextProperty europeImageNameProp;
  private INDITextElement europeImageNameElem;
  /**
   * The thread that continuously reads images and sends them back tothe
   * clients.
   */
  private Thread runningThread;
  /**
   * A signal to stop the thread
   */
  private boolean stop;

  /**
   * Initializes the driver. It creates the Proerties and its Elements.
   *
   * @param inputStream The input stream from which the Driver will read.
   * @param outputStream The output stream to which the Driver will write.
   */
  public INDIElTiempoDriver(InputStream inputStream, OutputStream outputStream) {
    super(inputStream, outputStream);

    // We create the Switch Property with only one Switch Element

    send = new INDISwitchProperty(this, "SEND", "Send Image", "Main Control", PropertyStates.IDLE, PropertyPermissions.RW, 3, SwitchRules.AT_MOST_ONE);
    sendImage = new INDISwitchElement(send, "SEND", "Send Image", SwitchStatus.OFF);

    addProperty(send);

    // We create the BLOB Property for the Spain satellite image
    spainImageProp = new INDIBLOBProperty(this, "SPAIN_SATELLITE_IMAGE", "Spain Image", "Main Control", PropertyStates.IDLE, PropertyPermissions.RO, 0);
    spainImageElem = new INDIBLOBElement(spainImageProp, "SPAIN_SATELLITE_IMAGE", "Spain Image");

    addProperty(spainImageProp);

    // We create the Text Property for the Spain image name
    spainImageNameProp = new INDITextProperty(this, "SPAIN_IMAGE_NAME", "Spain Image Name", "Main Control", PropertyStates.IDLE, PropertyPermissions.RO, 3);
    spainImageNameElem = new INDITextElement(spainImageNameProp, "SPAIN_IMAGE_NAME", "Spain Image Name", "");

    addProperty(spainImageNameProp);

    // We create the BLOB Property for the Europe satellite image
    europeImageProp = new INDIBLOBProperty(this, "EUROPE_SATELLITE_IMAGE", "Europe Image", "Main Control", PropertyStates.IDLE, PropertyPermissions.RO, 0);
    europeImageElem = new INDIBLOBElement(europeImageProp, "EUROPE_SATELLITE_IMAGE", "Europe Image");

    addProperty(europeImageProp);

    // We create the Text Property for the Europe image name
    europeImageNameProp = new INDITextProperty(this, "EUROPE_IMAGE_NAME", "Europe Image Name", "Main Control", PropertyStates.IDLE, PropertyPermissions.RO, 3);
    europeImageNameElem = new INDITextElement(europeImageNameProp, "EUROPE_IMAGE_NAME", "Europe Image Name", "");

    addProperty(europeImageNameProp);

    stop = true;
  }

  /**
   * Gets the name of the Driver
   */
  @Override
  public String getName() {
    return "El Tiempo INDI Driver";
  }

  /**
   * Nothing happens as there are no writable Text Properties
   *
   * @param property
   * @param timestamp
   * @param elementsAndValues
   */
  @Override
  public void processNewTextValue(INDITextProperty property, Date timestamp, INDITextElementAndValue[] elementsAndValues) {
  }

  /**
   * If we receive the Switch Value ON of the property "SEND" we check for new
   * images in the web, download them and send them to the client.
   *
   * @param property
   * @param timestamp
   * @param elementsAndValues
   */
  @Override
  public void processNewSwitchValue(INDISwitchProperty property, Date timestamp, INDISwitchElementAndValue[] elementsAndValues) {

    if (property == send) {

      if (elementsAndValues.length > 0) {
        SwitchStatus stat = elementsAndValues[0].getValue();

        if (stat == SwitchStatus.ON) {

          property.setState(PropertyStates.OK);

          try {
            updateProperty(property, "Checking images");
          } catch (INDIException e) {
            e.printStackTrace();
            System.exit(-1);
          }

          checksForSpainImage(true);

          checksForEuropeImage(true);
        }
      }
    }
  }

  /**
   * Checks for the Spain Image and, if new, sends it to the clients.
   *
   * @param alwaysSend if <code>true</code> the image is sended to the client.
   * If not, it is only sended if it is new.
   */
  private void checksForSpainImage(boolean alwaysSend) {
    boolean newImage = checkForImage("http://www.eltiempo.es/satelite", "SPAIN");
    INDIBLOBValue v = spainImageElem.getValue();

    if (v.getSize() > 0) {
      if (newImage || alwaysSend) {
        spainImageProp.setState(PropertyStates.OK);

        spainImageNameProp.setState(PropertyStates.OK);

        try {
          updateProperty(spainImageProp);
          updateProperty(spainImageNameProp);
        } catch (INDIException e) {
          e.printStackTrace();
          System.exit(-1);
        }
      }
    }
  }

  private void checksForEuropeImage(boolean alwaysSend) {
    boolean newImage = checkForImage("http://www.eltiempo.es/europa/satelite/", "EUROPE");
    INDIBLOBValue v = europeImageElem.getValue();

    if (v.getSize() > 0) {
      if (newImage || alwaysSend) {
        europeImageProp.setState(PropertyStates.OK);

        europeImageNameProp.setState(PropertyStates.OK);

        try {
          updateProperty(europeImageProp);
          updateProperty(europeImageNameProp);
        } catch (INDIException e) {
          e.printStackTrace();
          System.exit(-1);
        }
      }
    }
  }

  /**
   * Nothing happens as there are no writable Number Properties
   *
   * @param property
   * @param timestamp
   * @param elementsAndValues
   */
  @Override
  public void processNewNumberValue(INDINumberProperty property, Date timestamp, INDINumberElementAndValue[] elementsAndValues) {
  }

  /**
   * Nothing happens as there are no writable BLOB Properties
   *
   * @param property
   * @param timestamp
   * @param elementsAndValues
   */
  @Override
  public void processNewBLOBValue(INDIBLOBProperty property, Date timestamp, INDIBLOBElementAndValue[] elementsAndValues) {
  }

  /**
   * Checks for a new image in the
   * <code>url</code> and if it has changed, saves it to the appropriate BLOB
   * Property (data) and Text Property (name) - according to the
   * <code>imagePrefix</code>.
   */
  private boolean checkForImage(String url, String imagePrefix) {
    File webpage = new File("web.html");
    String text;

    try {
      downloadAndSave(url, webpage);

      text = readFile(webpage);
    } catch (IOException e) {
      e.printStackTrace();

      return false;
    }

    // We look for the URL of the image to be downloaded
    String searchString = "<img id=\"imgmap\" src=\"";
    int start = text.indexOf(searchString);

    if (start == -1) {  // Not found
      return false;
    }

    start += searchString.length();

    int stop = text.indexOf("\"", start + 1);

    if (stop == -1) {
      return false;
    }

    String imgURL = text.substring(start, stop);

    int lastBar = imgURL.lastIndexOf("/");

    String fileName = imgURL.substring(lastBar + 1);

    File image = new File(fileName);

    if (!image.exists()) {  // Download the image
      try {
        downloadAndSave(imgURL, image);
      } catch (IOException e) {
        e.printStackTrace();

        return false;
      }
    }

    byte[] imageBytes;

    try {
      imageBytes = readBinaryFile(image);
    } catch (IOException e) {
      e.printStackTrace();

      return false;
    }

    // Define
    INDIBLOBProperty pim = (INDIBLOBProperty) getProperty(imagePrefix + "_SATELLITE_IMAGE");
    INDIBLOBElement eim = (INDIBLOBElement) pim.getElement(imagePrefix + "_SATELLITE_IMAGE");

    if (Arrays.equals(imageBytes, eim.getValue().getBLOBData())) {
      return false;  // The same image as the one in the property
    }

    eim.setValue(new INDIBLOBValue(imageBytes, "jpg"));

    int pos1 = fileName.lastIndexOf("-");

    String name = fileName.substring(pos1, pos1 + 5) + "/" + fileName.substring(pos1 + 5, pos1 + 7) + "/" + fileName.substring(pos1 + 7, pos1 + 9) + " " + fileName.substring(pos1 + 9, pos1 + 11) + ":" + fileName.substring(pos1 + 11, pos1 + 13);

    INDITextProperty pn = (INDITextProperty) getProperty(imagePrefix + "_IMAGE_NAME");
    INDITextElement en = (INDITextElement) pn.getElement(imagePrefix + "_IMAGE_NAME");

    en.setValue(imagePrefix + " Satellite " + name + " UTC");

    return true;
  }

  /**
   * Reads a text file and returns its contents as a String.
   *
   * @param file The file to read
   * @return The contents of the file
   * @throws IOException
   */
  private String readFile(File file) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(file));
    String line;
    StringBuilder stringBuilder = new StringBuilder();
    String ls = System.getProperty("line.separator");

    while ((line = reader.readLine()) != null) {
      stringBuilder.append(line);
      stringBuilder.append(ls);
    }

    return stringBuilder.toString();
  }

  /**
   * Reads a binary file and returns its contents as a byte[]
   *
   * @param file The file to be read
   * @return The contents of the file
   * @throws IOException
   */
  private byte[] readBinaryFile(File file) throws IOException {
    int fileSize = (int) file.length();
    FileInputStream reader = new FileInputStream(file);

    byte[] buffer = new byte[fileSize];

    int totalRead = 0;

    while (totalRead < fileSize) {
      int readed = reader.read(buffer, totalRead, fileSize - totalRead);

      if (readed == -1) {
        return null; // Unexpected end of file 
      }

      totalRead += readed;
    }

    return buffer;
  }

  /**
   * Downloads a
   * <code>url</code> and saves its contents to
   * <code>file</code>.
   *
   * @param url
   * @param file
   * @throws IOException
   * @throws MalformedURLException
   */
  private void downloadAndSave(String url, File file) throws IOException, MalformedURLException {
    int bufsize = 65536;
    byte[] buffer = new byte[bufsize];

    URL u = new URL(url);
    InputStream is = u.openStream();  // throws an IOException
    BufferedInputStream bis = new BufferedInputStream(is);
    FileOutputStream fos = new FileOutputStream(file);

    int readed = 0;

    while (readed != -1) {
      readed = bis.read(buffer);

      if (readed > 0) {
        fos.write(buffer, 0, readed);
      }
    }

    bis.close();
    fos.close();
  }

  /**
   * The thread that every 15 minutes checks for new images and, if they have
   * changed, downloads them and sends them back to the clients.
   */
  @Override
  public void run() {
    while (!stop) {
      try {
        Thread.sleep(15 * 60 * 1000);
      } catch (InterruptedException e) {
      }

      if (!stop) {
        checksForSpainImage(false);

        checksForEuropeImage(false);
      }
    }

    printMessage("Thread Stopped");
  }

  /**
   * Creates a new thread which reads new images every 15 minutes and sends them
   * back to the clients.
   *
   * @param timestamp
   * @throws INDIException
   * @see #run()
   */
  @Override
  public void driverConnect(Date timestamp) throws INDIException {
    if (stop == true) {
      printMessage("Starting El Tiempo Driver");
      stop = false;
      runningThread = new Thread(this);
      runningThread.start();
    }
  }

  @Override
  public void driverDisconnect(Date timestamp) throws INDIException {
    printMessage("Stopping El Tiempo Driver");

    stop = true;
  }
}
