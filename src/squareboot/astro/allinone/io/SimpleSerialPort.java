package squareboot.astro.allinone.io;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortException;

/**
 * Serial port that notifies the listeners every time it receives a char.
 *
 * @author SquareBoot
 * @version 0.1
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class SimpleSerialPort extends GenericSerialPort {

    /**
     * Class constructor.
     */
    public SimpleSerialPort() {
        super();
    }

    /**
     * Class constructor. Initializes the serial port and starts a connection.
     *
     * @param port the port of your board.
     */
    public SimpleSerialPort(String port) {
        super(port);
    }

    /**
     * Class constructor. Initializes the serial port and starts a connection.
     *
     * @param port the port of your board.
     * @param rate the baud rate.
     */
    public SimpleSerialPort(String port, int rate) {
        super(port, rate);
    }

    /**
     * @return a mask for this serial port.
     */
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
            notifyListener(serialPort.readString());

        } catch (SerialPortException e) {
            notifyError(new ConnectionError("An error occurred while receiving data from the serial port!",
                    e, ConnectionError.Type.INPUT));
        }
    }
}