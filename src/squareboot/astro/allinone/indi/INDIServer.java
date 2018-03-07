/*
 *  This file is part of INDI for Java Server.
 *
 *  INDI for Java Server is free software: you can redistribute it
 *  and/or modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation, either version 3 of
 *  the License, or (at your option) any later version.
 *
 *  INDI for Java Server is distributed in the hope that it will be
 *  useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 *  of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with INDI for Java Server.  If not, see
 *  <http://www.gnu.org/licenses/>.
 */
package squareboot.astro.allinone.indi;

import laazotea.indi.INDIException;
import laazotea.indi.driver.INDIDriver;
import laazotea.indi.server.DefaultINDIServer;
import laazotea.indi.server.INDIClient;
import laazotea.indi.server.INDIDevice;
import squareboot.astro.allinone.io.ConnectionError;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A simple INDI Server that basically sends all messages from drivers and
 * clients and viceversa, just performing basic checks of messages integrity.
 * It allows to dinamically load / unload different kinds of Devices with simple
 * shell commands.
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @author SquareBoot
 * @version 2.0
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class INDIServer extends DefaultINDIServer {

    /**
     * Class constructor.
     */
    public INDIServer() {
        super();
    }

    /**
     * Class constructor.
     *
     * @param port The port to which the server will listen.
     */
    public INDIServer(int port) {
        super(port);
    }

    /**
     * Gets the list of loaded devices.
     *
     * @return The list of loaded devices.
     */
    @Override
    public ArrayList<INDIDevice> getDevices() {
        return super.getDevices();
    }

    /**
     * Loads a Java driver from a JAR file
     *
     * @param jar The JAR file
     * @see #unloadJar
     * @see #reloadJar(String)
     */
    public void loadJar(String jar) {
        try {
            loadJavaDriversFromJAR(jar);

        } catch (INDIException e) {
            throw new ConnectionError("Error during driver loading!", ConnectionError.Type.IO);
        }
    }

    /**
     * Unloads a Java driver from its JAR file.
     *
     * @param jar The JAR file.
     * @see #loadJar
     * @see #reloadJar(String)
     */
    public void unloadJar(String jar) {
        destroyJavaDriversFromJAR(jar);
    }

    /**
     * Reloads a Java driver from its JAR file.
     *
     * @param jar the JAR file.
     * @see #loadJar(String)
     * @see #unloadJar(String)
     */
    private void reloadJar(String jar) {
        unloadJar(jar);
        try {
            Thread.sleep(100);
            while (isAlreadyLoaded(jar)) {
                Thread.sleep(100);
            }

        } catch (InterruptedException ignored) {

        }
        loadJar(jar);
    }

    /**
     * Loads a Java driver from its class.
     *
     * @param driver a Java driver.
     */
    public void loadJava(Class<? extends INDIDriver> driver) {
        try {
            loadJavaDriver(driver);

        } catch (INDIException e) {
            throw new ConnectionError("Error during driver loading!", ConnectionError.Type.IO);
        }
    }

    /**
     * Unloads a Java driver from its class.
     *
     * @param driver a Java driver.
     */
    public void unloadJava(Class<? extends INDIDriver> driver) {
        destroyJavaDriver(driver);
    }

    /**
     * Loads a native driver.
     *
     * @param path the path of the driver.
     * @see #unloadNative(String)
     * @see #reloadNative(String)
     */
    public void loadNative(String path) {
        try {
            loadNativeDriver(path);

        } catch (INDIException e) {
            throw new ConnectionError("Error during driver loading!", ConnectionError.Type.IO);
        }
    }

    /**
     * Unloads a native driver.
     *
     * @param path the path of the driver.
     * @see #loadNative
     * @see #reloadNative(String)
     */
    public void unloadNative(String path) {
        destroyNativeDriver(path);
    }

    /**
     * Reloads a native driver from its path.
     *
     * @param name the name of the driver.
     * @see #loadNative(String)
     * @see #unloadNative(String)
     */
    private void reloadNative(String name) {
        unloadNative(name);
        try {
            Thread.sleep(100);
            while (isAlreadyLoaded(name)) {
                Thread.sleep(100);
            }

        } catch (InterruptedException ignored) {

        }
        unloadNative(name);
    }

    /**
     * Starts the listening thread.
     */
    @Override
    protected void startListeningToClients() {
        if (isServerRunning()) {
            throw new ConnectionError("Server already started!", ConnectionError.Type.ALREADY_STARTED);

        } else {
            super.startListeningToClients();
        }
    }

    /**
     * Gets if the server is listening for new Clients to connect.
     *
     * @return <code>true</code> if the server is listening for new Clients. <code>false</code> otherwise.
     */
    @Override
    public boolean isServerRunning() {
        return super.isServerRunning();
    }

    /**
     * Stops the server from listening new clients. All connections with existing
     * clients are also broken.
     */
    @Override
    protected void stopServer() {
        if (isServerRunning()) {
            super.stopServer();

        } else {
            throw new ConnectionError("Server not started!", ConnectionError.Type.NOT_STARTED);
        }
    }

    /**
     * Connects to another Server.
     *
     * @param host The host of the other derver.
     * @param port The port of the other derver.
     * @see #disconnect
     */
    public void connect(String host, int port) {
        try {
            loadNetworkDriver(host, port);

        } catch (INDIException e) {
            throw new ConnectionError("Unable to connect to remove server!", e, ConnectionError.Type.CONNECTION);
        }
    }

    /**
     * Disconnects from another Server.
     *
     * @param host The host of the other server.
     * @param port The port of the other server.
     * @see #connect
     */
    public void disconnect(String host, int port) {
        destroyNetworkDriver(host, port);
    }

    /**
     * Prints a message about the broken connection to the standard err.
     *
     * @param client The Client whose connection has been broken
     */
    @Override
    protected void connectionWithClientBroken(INDIClient client) {
        System.err.println("Connection with client " + client.getInetAddress() + " has been broken.");
    }

    /**
     * Prints a message about the established connection to the standard err.
     *
     * @param client The Client whose connection has been established
     */
    @Override
    protected void connectionWithClientEstablished(INDIClient client) {
        System.err.println("Connection with client " + client.getInetAddress() + " established.");
    }

    /**
     * Prints a message about the driver which has been disconnected.
     *
     * @param driverIdentifier the driver identifier.
     * @param deviceNames      its devices.
     */
    @Override
    protected void driverDisconnected(String driverIdentifier, String[] deviceNames) {
        System.err.println("Driver " + driverIdentifier + " has been disconnected. " +
                "The following devices have disappeared: " + Arrays.toString(deviceNames)
                .replace("[", "").replace("]", ""));
    }

    /**
     * Accepts the client if it is 127.0.0.1 (localhost).
     *
     * @param socket
     * @return <code>true</code> if it is the 127.0.0.1 host.
     */
    @Override
    protected boolean acceptClient(Socket socket) {
        //TODO(squareboot): implement this
        byte[] address = socket.getInetAddress().getAddress();
        return (address[0] == 127) && (address[1] == 0) && (address[2] == 0) && (address[3] == 1);
    }
}