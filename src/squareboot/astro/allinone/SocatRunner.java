package squareboot.astro.allinone;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @author SquareBoot
 * @version 0.1
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class SocatRunner implements Runnable {

    /**
     * The socat process.
     */
    private Process process;
    /**
     * Port 1.
     */
    private String port1;
    /**
     * Port 2.
     */
    private String port2;

    @Override
    public void run() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("socat",
                    "-d", "-d", "pty,raw,echo=0", "pty,raw,echo=0");
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                if (line.contains("] ")) {
                    line = line.substring(line.indexOf("] ") + 2);
                    System.out.println("socat says: " + line);
                    if (line.startsWith("N PTY is ")) {
                        line = line.replace("N PTY is ", "");
                        if (port1 == null) {
                            port1 = line;

                        } else if (port2 == null) {
                            port2 = line;
                            break;
                        }
                    }
                }
            }
            process.waitFor();
            in.close();

        } catch (Exception e) {
            System.err.println("Some errors occurred while launching socat: " + e.getMessage());
            e.printStackTrace();
            Main.exit(Main.ExitCodes.SOCAT_ERROR);
        }
    }

    /**
     * @return {@code true} if both the ports have been initialized.
     */
    public boolean isReady() {
        return (port1 != null) && (port2 != null);
    }

    /**
     * Stops the process.
     */
    public void stop() {
        process.destroy();
        System.out.println("socat stopped!");
    }

    /**
     * @return the socat process.
     */
    public Process getProcess() {
        return process;
    }

    /**
     * @return the first port.
     */
    public String getPort1() {
        return port1;
    }

    /**
     * @return the second port.
     */
    public String getPort2() {
        return port2;
    }
}