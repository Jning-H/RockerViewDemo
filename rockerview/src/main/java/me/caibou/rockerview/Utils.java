package me.caibou.rockerview;

/**
 * @author caibou
 */
public class Utils {

    public static boolean range(double value, double min, double max) {
        if (min < value && value <= max) {
            return true;
        }
        return false;
    }

}
