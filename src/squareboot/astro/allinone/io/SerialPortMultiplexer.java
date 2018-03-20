package squareboot.astro.allinone.io;

import squareboot.astro.allinone.SocatRunner;

/**
 * @author SquareBoot
 * @version 0.1
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class SerialPortMultiplexer {

    /**
     * Socat.
     */
    private SocatRunner socat;
    /**
     * The mocked serial port.
     */
    private SimpleSerialPort mockedSerialPort;
    private Forwarder mockedSerialPortListener;
    /**
     * The real serial port.
     */
    private SimpleSerialPort realSerialPort;
    private Forwarder realSerialPortLister;

    /**
     * Class constructor.
     *
     * @param realSerialPort a real serial port.
     */
    public SerialPortMultiplexer(SimpleSerialPort realSerialPort) {
        socat = new SocatRunner();
        Thread thread = new Thread(socat);
        thread.start();
        // Wait for process to start
        try {
            long startTime = System.currentTimeMillis();
            while ((socat.isNotReady()) && (System.currentTimeMillis() - startTime < 1000)) {
                Thread.sleep(50);
            }

        } catch (InterruptedException ignored) {

        }
        if (socat.isNotReady()) {
            throw new IllegalStateException("Unable to start socat!");
        }

        mockedSerialPort = new SimpleSerialPort(socat.getPort1());
        this.realSerialPort = realSerialPort;

        mockedSerialPortListener = new Forwarder(this.realSerialPort);
        mockedSerialPort.addListener(mockedSerialPortListener);
        realSerialPortLister = new Forwarder(mockedSerialPort);
        this.realSerialPort.addListener(realSerialPortLister);
    }

    /**
     * Stops everything.
     */
    public void stop() {
        realSerialPort.removeListener(realSerialPortLister);
        mockedSerialPort.removeListener(mockedSerialPortListener);
        try {
            realSerialPort.disconnect();

        } catch (ConnectionError e) {
            e.printStackTrace();
        }
        try {
            mockedSerialPort.disconnect();

        } catch (ConnectionError e) {
            e.printStackTrace();
        }
        socat.stop();
    }

    /**
     * @return the port to be used for another connected.
     */
    public String getMockedPort() {
        return socat.getPort2();
    }

    /**
     * @author SquareBoot
     * @version 0.1
     */
    private class Forwarder implements SerialMessageListener {

        private SimpleSerialPort forwardTo;

        /**
         * Class constructor.
         */
        public Forwarder(SimpleSerialPort forwardTo) {
            this.forwardTo = forwardTo;
        }

        /**
         * Called when a new message is received from the Arduino.
         *
         * @param msg the received message.
         */
        @Override
        public void onMessage(String msg) {
            forwardTo.print(msg);
        }

        /**
         * Called when an error occurs.
         *
         * @param e the {@code Exception}.
         */
        @Override
        public void onConnectionError(Exception e) {
            e.printStackTrace();
        }
    }
}