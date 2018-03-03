package squareboot.astro.allinone.indi;

import java.io.File;

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
     * @param identifier an identifier for this driver.
     */
    public NativeDriverDefinition(File param, String identifier) {
        super(param, identifier);
    }

    public File getPath() {
        return param;
    }

    public void setPath(File path) {
        this.param = path;
    }

    @Override
    public String toString() {
        return super.toString() + " @" + param.getName().toLowerCase();
    }
}