package squareboot.astro.allinone.io;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortException;

import java.util.ArrayList;

/**
 * Implementation of {@link GenericSerialPort} that handles messages which end with a new line char.
 *
 * @author SquareBoot
 * @version 0.1
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Arduino extends GenericSerialPort {

    /**
     * {@code true} to lock the listeners. Used by {@link #waitFor(Arduino.ConditionChecker, long)}.
     */
    private boolean listenersDetach = false;
    /**
     * Used by {@link #waitFor(Arduino.ConditionChecker, long)} to check if the condition has been verified.
     */
    private ConditionChecker checker;
    /**
     * Temporary message received from the board.
     */
    private String tmpMsg = "";

    /**
     * Class constructor.
     */
    public Arduino() {
        super();
    }

    /**
     * Class constructor. Initializes the serial port and starts a connection.
     *
     * @param port the port of your board.
     */
    public Arduino(String port) {
        super(port);
    }

    /**
     * Class constructor. Initializes the serial port and starts a connection.
     *
     * @param port the port of your board.
     * @param rate the baud rate.
     */
    public Arduino(String port, int rate) {
        super(port, rate);
    }

    @Override
    protected int getMask() {
        return SerialPort.MASK_RXCHAR;
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
            notifyError(new ConnectionError("An error occurred while receiving data from the serial port!",
                    e, ConnectionError.Type.INPUT));
        }
    }

    /**
     * Sends a message to all the stored serial message listeners.
     *
     * @param msg the message.
     */
    @Override
    protected void notifyListener0(String msg) {
        msg = msg.replace("\n", "").replace("\r", "");
        if (!msg.equals("")) {
            if (listenersDetach) {
                checker.check0(msg);

            } else {
                for (SerialMessageListener l : listeners) {
                    l.onMessage(msg);
                }
            }
        }
    }

    /**
     * Stops the listeners and the current thread until the last received message becomes the one required.
     *
     * @param checker the {@link ConditionChecker} object that manages the interrupt.
     * @param timeout how many milliseconds to wait at most.
     * @return the required message.
     * @see ConditionChecker
     * @see ConditionChecker#check(String)
     */
    public String waitFor(ConditionChecker checker, long timeout) {
        if (checker == null) {
            throw new IllegalArgumentException("Null condition checker!");
        }
        this.checker = checker;
        listenersDetach = true;
        long start = System.currentTimeMillis();
        while (listenersDetach) {
            // Stop thread and check the timeout
            if ((System.currentTimeMillis() - start) >= timeout) {
                listenersDetach = false;
                throw new ConnectionError(ConnectionError.Type.NO_RESPONSE);
            }
        }
        return this.checker.getWaitedMessage();
    }

    /**
     * Interface used by {@link #waitFor(ConditionChecker, long)} to check if the received message is the one required.
     * Indeed, {@link #waitFor(ConditionChecker, long)} stops the thread until the last received message becomes the one required.
     *
     * @author SquareBoot
     * @version 0.1
     */
    public abstract class ConditionChecker {

        /**
         * Stores the message if it is the one required.
         */
        private String waitedMessage;

        /**
         * @return the required message, if available.
         */
        public String getWaitedMessage() {
            return waitedMessage;
        }

        /**
         * Checks the condition (internal use only). If the message is OK, saves it and terminates the waiting state.
         */
        private void check0(String msg) {
            if (check(msg)) {
                waitedMessage = msg;
                listenersDetach = false;
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