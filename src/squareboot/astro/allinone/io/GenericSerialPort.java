package squareboot.astro.allinone.io;

import jssc.*;

import java.util.ArrayList;

/**
 * Abstract manager for serial ports with listeners.
 * Provides a simple way to connect your board, to send and to receive data and to get a list containing all the available ports.
 * For each error, this class will use the {@link ConnectionError} class to give you a better explanation of the error
 * (see {@link ConnectionError#getType()}, {@link ConnectionError#getCause()} and {@link ConnectionError#getMessage()}).
 *
 * @author SquareBoot
 * @version 0.1
 * @see <a href="https://github.com/scream3r/java-simple-serial-connector">jSSC on GitHub</a>
 * @see <a href="https://code.google.com/archive/p/java-simple-serial-connector/wikis/jSSC_examples.wiki">jSSC examples - Google Code Archive</a>
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class GenericSerialPort implements SerialPortEventListener {

    /**
     * List of all the listeners.
     */
    protected ArrayList<SerialMessageListener> listeners = new ArrayList<>();
    /**
     * An instance of the {@link SerialPort} class.
     */
    protected SerialPort serialPort;

    /**
     * Class constructor.
     */
    public GenericSerialPort() {

    }

    /**
     * Class constructor. Initializes the serial port and starts a connection.
     *
     * @param port the port of your board.
     */
    public GenericSerialPort(String port) {
        connect(port);
    }

    /**
     * Class constructor. Initializes the serial port and starts a connection.
     *
     * @param port the port of your board.
     * @param rate the baud rate.
     */
    public GenericSerialPort(String port, int rate) {
        connect(port, rate);
    }

    /**
     * Serial ports discovery.
     *
     * @return an array containing all the available and not busy ports.
     */
    public static String[] listAvailablePorts() {
        return SerialPortList.getPortNames();
    }

    /**
     * Returns the actual state of the board: connected or disconnected.
     *
     * @return {@code true} if the board is connected, {@code false} if otherwise.
     */
    public boolean isConnected() {
        return (serialPort != null) && serialPort.isOpened();
    }

    /**
     * Adds a listener to the list.
     *
     * @param listener a listener to add.
     */
    public void addListener(SerialMessageListener listener) {
        if (listeners.contains(listener)) {
            throw new IllegalArgumentException("Listener already in the list!");
        }
        listeners.add(listener);
    }

    /**
     * Removes a listener from the list.
     *
     * @param listener a listener to remove.
     */
    public void removeListener(SerialMessageListener listener) {
        listeners.remove(listener);
    }

    /**
     * @return a mask for this serial port.
     */
    protected abstract int getMask();

    /**
     * Connects an board to this object.
     *
     * @param port the port.
     * @param rate the baud rate.
     */
    public void connect(String port, int rate) {
        serialPort = new SerialPort(port);
        try {
            serialPort.openPort();
            serialPort.setParams(rate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            serialPort.setEventsMask(getMask());
            serialPort.addEventListener(this);

        } catch (SerialPortException e) {
            ConnectionError.Type type;
            switch (e.getExceptionType()) {
                case SerialPortException.TYPE_PORT_BUSY: {
                    type = ConnectionError.Type.PORT_BUSY;
                    break;
                }

                case SerialPortException.TYPE_PORT_ALREADY_OPENED: {
                    type = ConnectionError.Type.PORT_BUSY;
                    break;
                }


                case SerialPortException.TYPE_PORT_NOT_FOUND: {
                    type = ConnectionError.Type.PORT_NOT_FOUND;
                    break;
                }

                default: {
                    type = ConnectionError.Type.UNKNOWN;
                }
            }
            throw new ConnectionError("An error occurred during connection!", e, type);
        }
    }

    /**
     * Connects a board to this object (at the default rate of 115200).
     *
     * @param port the port.
     */
    public void connect(String port) {
        connect(port, SerialPort.BAUDRATE_115200);
    }

    /**
     * Disconnects from the Serial Port and clears the listeners list.
     *
     * @see #connect
     */
    public void disconnect() {
        try {
            if (!serialPort.closePort()) {
                throw new ConnectionError("Something went wrong during the disconnection!", ConnectionError.Type.UNABLE_TO_DISCONNECT);
            }
            listeners.clear();

        } catch (SerialPortException e) {
            throw new ConnectionError("Something went wrong during the disconnection!", e, ConnectionError.Type.UNABLE_TO_DISCONNECT);
        }
    }

    /**
     * Sends an error event to the listeners.
     *
     * @param e the exception to notify.
     */
    protected void notifyError(Exception e) {
        for (SerialMessageListener l : listeners) {
            l.onConnectionError(e);
        }
    }

    /**
     * Prints a {@code String} to the connected board.
     *
     * @param message the message you want to send.
     */
    public void print(String message) {
        new Thread(() -> {
            try {
                if (!serialPort.writeBytes(message.getBytes())) {
                    notifyError(new ConnectionError("An error occurred while sending the message!",
                            ConnectionError.Type.OUTPUT));
                }

            } catch (SerialPortException e) {
                ConnectionError.Type type;
                switch (e.getExceptionType()) {
                    case SerialPortException.TYPE_PORT_BUSY: {
                        type = ConnectionError.Type.BUSY;
                        break;
                    }

                    case SerialPortException.TYPE_PORT_NOT_OPENED: {
                        type = ConnectionError.Type.NOT_CONNECTED;
                        break;
                    }

                    default: {
                        type = ConnectionError.Type.UNKNOWN;
                    }
                }
                notifyError(new ConnectionError("An error occurred during data transfer!", e, type));
            }
        }, "Serial data sender").start();
    }

    /**
     * Prints an {@code int} to the board.
     *
     * @param number the message you want to send.
     */
    public void print(int number) {
        print(String.valueOf(number));
    }

    /**
     * Prints a character to the board.
     *
     * @param c the char you want to send.
     */
    public void print(char c) {
        print(String.valueOf(c));
    }

    /**
     * Prints a {@code double} to the board.
     *
     * @param d the number you want to send.
     */
    public void print(double d) {
        print(String.valueOf(d));
    }

    /**
     * Prints a {@code String} to the connected board.
     *
     * @param message the message you want to send.
     */
    public void println(String message) {
        print(message + "\n");
    }

    /**
     * Prints an {@code int} to the board.
     *
     * @param number the message you want to send.
     */
    public void println(int number) {
        println(String.valueOf(number));
    }

    /**
     * Prints a character to the board.
     *
     * @param c the char you want to send.
     */
    public void println(char c) {
        println(String.valueOf(c));
    }

    /**
     * Prints a {@code double} to the board.
     *
     * @param d the number you want to send.
     */
    public void println(double d) {
        println(String.valueOf(d));
    }

    /**
     * Returns the name of the serial port currently being used..
     *
     * @return the serial port's name.
     */
    public String getSerialPortName() {
        return serialPort.getPortName();
    }

    /**
     * @return a String representation of this object.
     */
    @Override
    public String toString() {
        return "SerialPort[" + (isConnected() ? (serialPort.getPortName()) : "false") + "]";
    }

    /**
     * Serial event. Receives data from the connected board.
     *
     * @param portEvent the port event.
     */
    @Override
    public abstract void serialEvent(SerialPortEvent portEvent);

    /**
     * Sends a message to all the stored serial message listeners.
     *
     * @param msg the message.
     */
    protected void notifyListener(String msg) {
        if ((msg != null) && (!msg.equals(""))) {
            notifyListener0(msg);
        }
    }

    /**
     * Sends a message to all the stored serial message listeners.
     *
     * @param msg the message.
     */
    protected void notifyListener0(String msg) {
        for (SerialMessageListener l : listeners) {
            l.onMessage(msg);
        }
    }
}