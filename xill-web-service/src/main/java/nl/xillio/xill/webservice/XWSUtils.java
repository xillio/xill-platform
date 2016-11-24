package nl.xillio.xill.webservice;

/**
 * Utility class for the Xill Web Service module.
 *
 * @author andrea.parrilli
 */
public class XWSUtils {
    public XWSUtils() throws IllegalAccessException {
        throw new IllegalAccessException("Please do not instantiate utility classes");
    }

    public static String getPresentWorkingDirectory() {
        return System.getProperty("user.dir");
    }
}
