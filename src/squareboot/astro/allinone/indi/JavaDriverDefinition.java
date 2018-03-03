package squareboot.astro.allinone.indi;

import laazotea.indi.driver.INDIDriver;

/**
 * @author SquareBoot
 * @version 0.1
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class JavaDriverDefinition extends DriverDefinition<String> {

    /**
     * Class constructor.
     */
    public JavaDriverDefinition() {

    }

    /**
     * Class constructor.
     *
     * @param param      the path to this driver.
     * @param identifier an identifier for this driver.
     */
    public JavaDriverDefinition(INDIDriver param, String identifier) {
        super(param.getClass().getName(), identifier);
    }

    public String getDriverClass() {
        return param;
    }

    @Override
    public String toString() {
        return super.toString() + " @" + param.getClass().getSimpleName().toLowerCase();
    }
}