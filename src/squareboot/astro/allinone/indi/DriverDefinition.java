package squareboot.astro.allinone.indi;

/**
 * @author SquareBoot
 * @version 0.1
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class DriverDefinition<I> {

    protected String identifier;
    protected I param;

    /**
     * Class constructor.
     */
    public DriverDefinition() {

    }

    /**
     * Class constructor.
     *
     * @param param      a path to this driver, class or something else.
     * @param identifier an identifier for this driver. Mustn't contain the "&" char.
     */
    public DriverDefinition(I param, String identifier) {
        this.param = param;
        if (identifier.contains("&")) {
            throw new IllegalArgumentException("Invalid identifier: " + identifier);

        } else {
            this.identifier = identifier;
        }
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String toString() {
        return identifier;
    }
}