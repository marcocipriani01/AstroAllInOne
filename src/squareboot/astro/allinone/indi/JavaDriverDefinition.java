package squareboot.astro.allinone.indi;

import laazotea.indi.driver.INDIDriver;

/**
 * @author SquareBoot
 * @version 0.1
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class JavaDriverDefinition extends DriverDefinition<Class<? extends INDIDriver>> {

    /**
     * Class constructor.
     */
    public JavaDriverDefinition() {

    }

    /**
     * Class constructor.
     *
     * @param param      the path to this driver.
     * @param identifier an identifier for this driver. Mustn't contain the "&" char.
     */
    public JavaDriverDefinition(Class<? extends INDIDriver> param, String identifier) {
        super(param, identifier);
    }

    public Class<? extends INDIDriver> getDriverClass() {
        return param;
    }

    @Override
    public String toString() {
        return super.toString() + " @" + param.getSimpleName();
    }
}