package squareboot.astro.allinone.io;

import jssc.*;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Manager for serial ports.
 * Provides a simple way to connect your board, to send and to receive data and to get a list containing all the available ports.
 * Manages listeners and also interpreters (if supported).
 * To simplify the management, this class doesn't forward all the exceptions from the serial port's library as they are:
 * for each error, this class will use the {@link ConnectionError} class to give you a better explanation of the error
 * (see {@link ConnectionError#getType()}, {@link ConnectionError#getCause()} and {@link ConnectionError#getMessage()}).
 *
 * @author SquareBoot
 * @version 0.1
 * @see <a href="https://github.com/scream3r/java-simple-serial-connector">jSSC on GitHub</a>
 * @see <a href="https://code.google.com/archive/p/java-simple-serial-connector/wikis/jSSC_examples.wiki">jSSC examples - Google Code Archive</a>
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Arduino implements SerialPortEventListener, WithListeners<MessageListener<String>> {

    /**
     * List of all the listeners.
     */
    private ArrayList<MessageListener<String>> listeners = new ArrayList<>();
    /**
     * {@code true} to lock the listeners. Used by {@link #waitFor(ConditionChecker, long)}.
     */
    private boolean listenersDetach = false;
    /**
     * Used by {@link #waitFor(ConditionChecker, long)} to check if the condition has been verified.
     */
    private ConditionChecker checker;
    /**
     * The jSSC' SerialPort object instance.
     */
    private SerialPort serialPort;
    /**
     * Temporary message received from the board.
     */
    private String tmpMsg = "";

    /**
     * Class constructor.
     */
    public Arduino() {

    }

    /**
     * Class constructor. Initializes the serial port and starts a connection.
     *
     * @param port the port of your board.
     */
    public Arduino(String port) throws ConnectionError {
        connect(port);
    }

    /**
     * Serial ports discovery.
     *
     * @return an {@code ArrayList<CommPortIdentifier>} containing all the available and not busy ports.
     */
    public static ArrayList<String> scanForPorts() {
        return new ArrayList<>(Arrays.asList(SerialPortList.getPortNames()));
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
     * Adds a listener to your class.
     *
     * @param serialMessageListener your main class.
     */
    @Override
    public void addListener(MessageListener<String> serialMessageListener) {
        listeners.add(serialMessageListener);
    }

    /**
     * Connects an board to this object.
     *
     * @param port the port.
     * @param rate the baud rate.
     */
    public void connect(String port, int rate) throws ConnectionError {
        serialPort = new SerialPort(port);
        try {
            serialPort.openPort();
            serialPort.setParams(rate, 8, 1, 0);
            serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
            serialPort.addEventListener(this);

        } catch (SerialPortException e) {
            ConnectionError.Type type;
            switch (e.getExceptionType()) {
                case SerialPortException.TYPE_PORT_BUSY: {
                    type = ConnectionError.Type.CONNECTION_PORT_BUSY;
                    break;
                }

                case SerialPortException.TYPE_PORT_ALREADY_OPENED: {
                    type = ConnectionError.Type.CONNECTION_PORT_BUSY;
                    break;
                }


                case SerialPortException.TYPE_PORT_NOT_FOUND: {
                    type = ConnectionError.Type.CONNECTION_PORT_NOT_FOUND;
                    break;
                }

                default: {
                    type = ConnectionError.Type.UNKNOWN_FATAL;
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
    public void connect(String port) throws ConnectionError {
        connect(port, 115200);
    }

    /**
     * Disconnects from the Serial Port and clears the listeners list.
     *
     * @see #connect
     */
    public void disconnect() throws ConnectionError {
        try {
            if (!serialPort.closePort()) {
                throw new ConnectionError("Something went wrong during the disconnection!", ConnectionError.Type.DISCONNECTION);
            }
            listeners.clear();

        } catch (SerialPortException e) {
            throw new ConnectionError("Something went wrong during the disconnection!", e, ConnectionError.Type.DISCONNECTION);
        }
    }

    /**
     * Prints a {@code String} to the connected board.
     *
     * @param message the message you want to send.
     */
    public void println(String message) {
        new Thread(() -> {
            try {
                if (!serialPort.writeBytes(message.getBytes())) {
                    maybeNotifyError(new ConnectionError("An error occurred while sending the message!",
                            ConnectionError.Type.TRANSFER_OUTPUT));
                }

            } catch (SerialPortException e) {
                ConnectionError.Type type;
                switch (e.getExceptionType()) {
                    case SerialPortException.TYPE_PORT_BUSY: {
                        type = ConnectionError.Type.BUSY;
                        break;
                    }

                    case SerialPortException.TYPE_PORT_NOT_OPENED: {
                        type = ConnectionError.Type.TRANSFER_IO;
                        break;
                    }

                    default: {
                        type = ConnectionError.Type.UNKNOWN;
                    }
                }
                maybeNotifyError(new ConnectionError("An error occurred during data transfer!", e, type));
            }
        }, "Serial data sender").start();
    }

    /**
     * This function stops the listeners and the current thread until the last received message becomes the one required.
     * The condition is checked using {@link ConditionChecker}, so call it writing <br>
     * {@code arduino.waitFor(arduino.new ConditionChecker() {...})}
     *
     * @param checker the {@link ConditionChecker} object that manages the interrupt.
     * @param timeout how many milliseconds to wait at most.
     * @return the required message.
     * @see ConditionChecker
     * @see ConditionChecker#check(String)
     */
    public String waitFor(ConditionChecker checker, long timeout) throws ConnectionError {
        if (checker == null) {
            throw new IllegalArgumentException("Null condition checker!");
        }
        this.checker = checker;
        listenersDetach = true;
        long time0 = System.currentTimeMillis();
        while (listenersDetach) {
            // Stop thread
            // and check the timeout
            if ((System.currentTimeMillis() - time0) >= timeout) {
                listenersDetach = false;
                throw new ConnectionError(ConnectionError.Type.NO_RESPONSE);
            }
        }
        return this.checker.getWaitedMessage();
    }

    /**
     * Maybe sends an error event to the listeners: for each listener,
     * if it is an instance of the {@code SerialMessageListener} interface, this method will notify it about the given exception.
     *
     * @param e the exception to notify.
     */
    private void maybeNotifyError(Exception e) {
        for (MessageListener<String> l : listeners) {
            if (l instanceof SerialMessageListener) {
                ((SerialMessageListener) l).onConnectionError(e);
            }
        }
    }

    /**
     * Prints an {@code int} to the Arduino.
     *
     * @param number the message you want to send.
     */
    public void println(int number) {
        println(String.valueOf(number));
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
        return "Arduino[" + (isConnected() ? (serialPort.getPortName()) : "false") + "]";
    }

    /**
     * Serial event. Receives data from the connected board.
     *
     * @param portEvent the port event.
     */
    @Override
    public void serialEvent(SerialPortEvent portEvent) {
        try {
            tmpMsg = tmpMsg + serialPort.readString();

            if (tmpMsg.contains("\n") || tmpMsg.contains("\r")) {
                String[] split = tmpMsg.split("[\\n\\r]");
                ArrayList<String> messages = new ArrayList<>();
                for (String s : split) {
                    s = s.trim();
                    if (!s.equals("")) {
                        messages.add(s);
                    }
                }

                int size = messages.size();
                if (size > 0) {
                    int i = 0;
                    while (i < (size - 1)) {
                        notifyListener(messages.get(i));
                        i++;
                    }

                    if (tmpMsg.endsWith("\n") || tmpMsg.endsWith("\r")) {
                        notifyListener(messages.get(size - 1));
                        tmpMsg = "";

                    } else {
                        tmpMsg = messages.get(size - 1);
                    }
                }
            }

        } catch (SerialPortException e) {
            maybeNotifyError(new ConnectionError("An error occurred while receiving data from the serial port!",
                    e, ConnectionError.Type.TRANSFER_INPUT));
        }
    }

    /**
     * Sends a message to all the stored serial message listeners.
     *
     * @param msg the message.
     */
    private void notifyListener(String msg) {
        if (msg != null) {
            msg = msg.replace("\n", "").replace("\r", "");
            if (!msg.equals("")) {
                if (!listenersDetach) {
                    for (MessageListener<String> l : listeners) {
                        l.onMessage(msg);
                    }

                } else {
                    if (checker.check(msg)) {
                        listenersDetach = false;
                    }
                }
            }
        }
    }

    /**
     * Interface used by {@link #waitFor(ConditionChecker, long)} to check if the received message is the one required.
     * Indeed, {@link #waitFor(ConditionChecker, long)} stops the listeners for a while until the last received message becomes the one required.
     *
     * @author SquareBoot
     * @version 0.1
     */
    public abstract class ConditionChecker {

        /**
         * String that stores the message if it is the one required.
         */
        private String waitedMessage;
        /**
         * OK flag.
         */
        private boolean ok = false;

        /**
         * @return the required message, if available.
         * @throws IllegalStateException if there isn't any available message.
         */
        public String getWaitedMessage() {
            if (!ok) {
                throw new IllegalStateException("No message available!");
            }
            return waitedMessage;
        }

        /**
         * Checks the condition (internal use only). If the message is OK, saves it and terminates the waiting state.
         */
        private boolean check0(String msg) {
            if (check(msg)) {
                waitedMessage = msg;
                ok = true;
                return true;

            } else {
                return false;
            }
        }

        /**
         * Method to implement in order to check the condition.
         * Return {@code true} if the given message, which is the one received, is the one required; {@code false} if otherwise.
         *
         * @param msg the input message.
         * @return {@code true} if the message is the one required.
         */
        protected abstract boolean check(String msg);
    }
}