package james.signalstrengthslib;

public class SignalUtils {

    public static int getDbmLevel(int dbm) {
        if (dbm < -100) return 0;
        else if (dbm < -95) return 1;
        else if (dbm < -85) return 2;
        else if (dbm < -75) return 3;
        else if (dbm != 0) return 4;
        else return -1;
    }

    public static int getEcioLevel(int ecio) {
        if (ecio >= -90) return 4;
        else if (ecio >= -110) return 3;
        else if (ecio >= -130) return 2;
        else if (ecio >= -150) return 1;
        else return 0;
    }

    public static int getSnrLevel(int snr) {
        return snr / 2;
    }

    public static int getAsuLevel(int asu) {
        if (asu == 99) return -1;
        else return (2 * asu) - 113;
    }

    public static int getRsrpLevel(int rsrp) {
        if (rsrp > -84) return 4;
        else if (rsrp > -102) return 3;
        else if (rsrp > -111) return 2;
        else if (rsrp > -112) return 1;
        else return 0;
    }

    public static int getRsrqLevel(int rsrq) {
        if (rsrq > -5) return 4;
        else if (rsrq > -10) return 2;
        else return 1;
    }

    public static boolean isValidLevel(double level) {
        return level >= 0 && level <= 4;
    }

}
