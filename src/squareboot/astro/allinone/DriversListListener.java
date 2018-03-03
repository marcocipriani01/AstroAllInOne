package squareboot.astro.allinone;

import squareboot.astro.allinone.indi.DriverDefinition;

/**
 * @author SquareBoot
 * @version 0.1
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public interface DriversListListener {

    /**
     * Called when a new driver has been added to the list.
     *
     * @param driverDefinition the definition of the new driver.
     */
    void onDriverAdd(DriverDefinition driverDefinition);

    /**
     * Called when a new driver has been removed from the list.
     *
     * @param driverDefinition the definition of the removed driver.
     */
    void onDriverRemove(DriverDefinition driverDefinition);
}