package james.signalstrengthslib;

import android.os.Build;
import android.telephony.SignalStrength;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SignalStrengths {

    public final static String METHOD_LEVEL = "getLevel";
    public final static String METHOD_DBM = "getDbm (reflection)";
    public final static String METHOD_GSM_LEVEL = "getGsmLevel (reflection)";
    public final static String METHOD_GSM = "getGsmSignalStrength";
    public final static String METHOD_GSM_DBM = "getGsmDbm (reflection)";
    public final static String METHOD_CDMA_LEVEL = "getCdmaLevel (reflection)";
    public final static String METHOD_EVDO_LEVEL = "getEvdoLevel (reflection)";
    public final static String METHOD_TD_SCDMA_LEVEL = "getTdScdmaLevel (reflection)";
    public final static String METHOD_TD_SCDMA_DBM = "getTdScdmaDbm (reflection)";
    public final static String METHOD_CDMA_DBM = "getCdmaDbm";
    public final static String METHOD_CDMA_ECIO = "getCdmaEcio";
    public final static String METHOD_EVDO_DBM = "getEvdoDbm";
    public final static String METHOD_EVDO_ECIO = "getEvdoEcio";
    public final static String METHOD_EVDO_SNR = "getEvdoSnr";
    public final static String METHOD_ASU_LEVEL = "getAsuLevel";
    public final static String METHOD_LTE_LEVEL = "getLteLevel (reflection)";
    public final static String METHOD_LTE_SIGNAL_STRENGTH = "getLteSignalStrength (reflection)";
    public final static String METHOD_LTE_DBM = "getLteDbm (reflection)";
    public final static String METHOD_LTE_RSRP = "getLteRsrp (reflection)";
    public final static String METHOD_LTE_RSRQ = "getLteRsrq (reflection)";
    public final static String METHOD_LTE_RSSNR = "getLteRssnr (reflection)";
    public final static String METHOD_LTE_CQI = "getLteCqi (reflection)";

    private static final List<SignalMethod> methods = new ArrayList<>(Arrays.asList(
            new SignalMethod(METHOD_LEVEL) {
                @Override
                public double getLevel(SignalStrength signalStrength) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        return signalStrength.getLevel();
                    else return -1;
                }
            },
            new SignalMethod(METHOD_DBM) {
                @Override
                public double getLevel(SignalStrength signalStrength) throws Exception {
                    Method method = signalStrength.getClass().getDeclaredMethod("getDbm");
                    method.setAccessible(true);
                    return SignalUtils.getDbmLevel((int) method.invoke(signalStrength));
                }
            },
            new SignalMethod(METHOD_GSM_LEVEL) {
                @Override
                public double getLevel(SignalStrength signalStrength) throws Exception {
                    Method method = signalStrength.getClass().getDeclaredMethod("getGsmLevel");
                    method.setAccessible(true);
                    return (int) method.invoke(signalStrength);
                }
            },
            new SignalMethod(METHOD_GSM) {
                @Override
                public double getLevel(SignalStrength signalStrength) {
                    return (int) ((signalStrength.getGsmSignalStrength() / 31.0) * 4);
                }
            },
            new SignalMethod(METHOD_GSM_DBM) {
                @Override
                public double getLevel(SignalStrength signalStrength) throws Exception {
                    Method method = signalStrength.getClass().getDeclaredMethod("getGsmDbm");
                    method.setAccessible(true);
                    return SignalUtils.getDbmLevel((int) method.invoke(signalStrength));
                }
            },
            new SignalMethod(METHOD_CDMA_LEVEL) {
                @Override
                public double getLevel(SignalStrength signalStrength) throws Exception {
                    Method method = signalStrength.getClass().getDeclaredMethod("getCdmaLevel");
                    method.setAccessible(true);
                    return (int) method.invoke(signalStrength);
                }
            },
            new SignalMethod(METHOD_EVDO_LEVEL) {
                @Override
                public double getLevel(SignalStrength signalStrength) throws Exception {
                    Method method = signalStrength.getClass().getDeclaredMethod("getEvdoLevel");
                    method.setAccessible(true);
                    return (int) method.invoke(signalStrength);
                }
            },
            new SignalMethod(METHOD_TD_SCDMA_LEVEL) {
                @Override
                public double getLevel(SignalStrength signalStrength) throws Exception {
                    Method method = signalStrength.getClass().getDeclaredMethod("getTdScdmaLevel");
                    method.setAccessible(true);
                    return (int) method.invoke(signalStrength);
                }

                @Override
                public boolean isExcluded() {
                    return isExcluded == null || isExcluded;
                }
            },
            new SignalMethod(METHOD_TD_SCDMA_DBM) {
                @Override
                public double getLevel(SignalStrength signalStrength) throws Exception {
                    Method method = signalStrength.getClass().getDeclaredMethod("getTdScdmaDbm");
                    method.setAccessible(true);
                    return SignalUtils.getDbmLevel((int) method.invoke(signalStrength));
                }

                @Override
                public boolean isExcluded() {
                    return isExcluded == null || isExcluded;
                }
            },
            new SignalMethod(METHOD_CDMA_DBM) {
                @Override
                public double getLevel(SignalStrength signalStrength) {
                    return SignalUtils.getDbmLevel(signalStrength.getCdmaDbm());
                }
            },
            new SignalMethod(METHOD_CDMA_ECIO) {
                @Override
                public double getLevel(SignalStrength signalStrength) {
                    return SignalUtils.getEcioLevel(signalStrength.getCdmaEcio());
                }
            },
            new SignalMethod(METHOD_EVDO_DBM) {
                @Override
                public double getLevel(SignalStrength signalStrength) {
                    return SignalUtils.getDbmLevel(signalStrength.getEvdoDbm());
                }
            },
            new SignalMethod(METHOD_EVDO_ECIO) {
                @Override
                public double getLevel(SignalStrength signalStrength) {
                    return SignalUtils.getEcioLevel(signalStrength.getEvdoEcio());
                }
            },
            new SignalMethod(METHOD_EVDO_SNR) {
                @Override
                public double getLevel(SignalStrength signalStrength) {
                    return SignalUtils.getSnrLevel(signalStrength.getEvdoSnr());
                }

                @Override
                public boolean isExcluded() {
                    return isExcluded == null || isExcluded;
                }
            },
            new SignalMethod(METHOD_ASU_LEVEL) {
                @Override
                public double getLevel(SignalStrength signalStrength) throws Exception {
                    Method method = signalStrength.getClass().getDeclaredMethod("getAsuLevel");
                    method.setAccessible(true);
                    return SignalUtils.getAsuLevel((int) method.invoke(signalStrength));
                }
            },
            new SignalMethod(METHOD_LTE_LEVEL) {
                @Override
                public double getLevel(SignalStrength signalStrength) throws Exception {
                    Method method = signalStrength.getClass().getDeclaredMethod("getLteLevel");
                    method.setAccessible(true);
                    return (int) method.invoke(signalStrength);
                }

                @Override
                public boolean isExcluded() {
                    return isExcluded == null || isExcluded;
                }
            },
            new SignalMethod(METHOD_LTE_SIGNAL_STRENGTH) {
                @Override
                public double getLevel(SignalStrength signalStrength) throws Exception {
                    Method method = signalStrength.getClass().getDeclaredMethod("getLteSignalStrength");
                    method.setAccessible(true);
                    return (int) method.invoke(signalStrength);
                }

                @Override
                public boolean isExcluded() {
                    return isExcluded == null || isExcluded;
                }
            },
            new SignalMethod(METHOD_LTE_DBM) {
                @Override
                public double getLevel(SignalStrength signalStrength) throws Exception {
                    Method method = signalStrength.getClass().getDeclaredMethod("getLteDbm");
                    method.setAccessible(true);
                    return SignalUtils.getDbmLevel((int) method.invoke(signalStrength));
                }

                @Override
                public boolean isExcluded() {
                    return isExcluded == null || isExcluded;
                }
            },
            new SignalMethod(METHOD_LTE_RSRP) {
                @Override
                public double getLevel(SignalStrength signalStrength) throws Exception {
                    Method method = signalStrength.getClass().getDeclaredMethod("getLteRsrp");
                    method.setAccessible(true);
                    return SignalUtils.getRsrpLevel((int) method.invoke(signalStrength));
                }

                @Override
                public boolean isExcluded() {
                    return isExcluded == null || isExcluded;
                }
            },
            new SignalMethod(METHOD_LTE_RSRQ) {
                @Override
                public double getLevel(SignalStrength signalStrength) throws Exception {
                    Method method = signalStrength.getClass().getDeclaredMethod("getLteRsrq");
                    method.setAccessible(true);
                    return SignalUtils.getRsrqLevel((int) method.invoke(signalStrength));
                }

                @Override
                public boolean isExcluded() {
                    return isExcluded == null || isExcluded;
                }
            },
            new SignalMethod(METHOD_LTE_RSSNR) {
                @Override
                public double getLevel(SignalStrength signalStrength) throws Exception {
                    Method method = signalStrength.getClass().getDeclaredMethod("getLteRssnr");
                    method.setAccessible(true);
                    return SignalUtils.getSnrLevel((int) method.invoke(signalStrength));
                }

                @Override
                public boolean isExcluded() {
                    return isExcluded == null || isExcluded;
                }
            },
            new SignalMethod(METHOD_LTE_CQI) {
                @Override
                public double getLevel(SignalStrength signalStrength) throws Exception {
                    Method method = signalStrength.getClass().getDeclaredMethod("getLteCqi");
                    method.setAccessible(true);
                    return (int) method.invoke(signalStrength);
                }

                @Override
                public boolean isExcluded() {
                    return isExcluded == null || isExcluded;
                }
            }
    ));

    public static List<SignalMethod> getMethods() {
        return methods;
    }

    public static double get(String methodName, SignalStrength signalStrength) {
        for (SignalMethod method : methods) {
            if (methodName.equals(method.getName()))
                try {
                    return method.getLevel(signalStrength);
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
        }

        return 0;
    }

    public static double getFirstValid(SignalStrength signalStrength) {
        for (SignalMethod method : methods) {
            if (method.isExcluded()) continue;

            double level;
            try {
                level = method.getLevel(signalStrength);
            } catch (Exception e) {
                continue;
            }

            if (SignalUtils.isValidLevel(level) && level > 0)
                return level;
        }

        return 0;
    }

    public static double getAverage(SignalStrength signalStrength) {
        List<Double> values = new ArrayList<>();

        for (SignalMethod method : methods) {
            if (method.isExcluded()) continue;

            double level;
            try {
                level = method.getLevel(signalStrength);
            } catch (Exception e) {
                continue;
            }

            if (SignalUtils.isValidLevel(level) && level > 0)
                values.add(level);
        }

        double level = 0;
        for (Double value : values) {
            level += value;
        }

        return values.size() > 0 ? level / values.size() : level;
    }

}
