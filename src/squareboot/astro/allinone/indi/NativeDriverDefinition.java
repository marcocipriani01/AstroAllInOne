package squareboot.astro.allinone.indi;

import laazotea.indi.server.INDIDevice;
import laazotea.indi.server.INDINativeDevice;

import java.io.File;
import java.util.ArrayList;

/**
 * @author SquareBoot
 * @version 0.1
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class NativeDriverDefinition extends DriverDefinition<File> {

    /**
     * Class constructor.
     */
    public NativeDriverDefinition() {

    }

    /**
     * Class constructor.
     *
     * @param param      the path to this driver.
     * @param identifier an identifier for this driver. Mustn't contain the "&" char.
     */
    public NativeDriverDefinition(File param, String identifier) {
        super(param, identifier);
    }

    /**
     * Tries to get a name
     *
     * @param server a server.
     */
    public static String processIdentifier(String ndf, INDIServer server) {
        ArrayList<INDIDevice> oldDevices = server.getDevices();
        server.loadNative(ndf);
        ArrayList<INDIDevice> newDevices = server.getDevices();
        ArrayList<INDIDevice> delta = new ArrayList<>();
        for (INDIDevice d : newDevices) {
            if (!oldDevices.contains(d)) {
                delta.add(d);
            }
        }
        if (delta.size() == 1) {
            INDIDevice device = delta.get(0);
            if (device instanceof INDINativeDevice) {
                String name = ((INDINativeDevice) device).getNames()[0];
                server.unloadNative(ndf);
                return name;

            } else {
                server.unloadNative(ndf);
                throw new DriverNotFoundException("Could not find a native driver!");
            }

        } else {
            server.unloadNative(ndf);
            throw new DriverNotFoundException("More or less than one device added after driver loading!");
        }
    }

    /**
     * @return the path of the driver represented by this definition.
     */
    public File getPath() {
        return param;
    }

    /**
     * @param path the new path of the driver represented by this definition.
     */
    public void setPath(File path) {
        this.param = path;
    }

    @Override
    public String toString() {
        return super.toString() + " @" + param.getName();
    }
}