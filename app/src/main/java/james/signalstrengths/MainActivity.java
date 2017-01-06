package james.signalstrengths;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TelephonyManager telephonyManager;
    private Listener listener;

    private List<SignalMethod> methods;
    private GridLayout gridLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gridLayout = (GridLayout) findViewById(R.id.gridLayout);

        methods = new ArrayList<>(Arrays.asList(
                new SignalMethod("Signal Level") {
                    @Override
                    double getLevel(SignalStrength signalStrength) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
                        List<Double> values = new ArrayList<>();
                        for (SignalMethod method : methods) {
                            if (method.isExcluded()) continue;

                            double level;
                            try {
                                level = method.getLevel(signalStrength);
                            } catch (Exception e) {
                                continue;
                            }

                            if (isValidLevel(level) && level > 0)
                                values.add(level);
                        }

                        double level = 0;
                        for (Double value : values) {
                            level += value;
                        }

                        return values.size() > 1 ? level / values.size() : level;
                    }

                    @Override
                    public boolean isExcluded() {
                        return true;
                    }
                },
                new SignalMethod("getLevel") {
                    @Override
                    double getLevel(SignalStrength signalStrength) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                            return signalStrength.getLevel();
                        else return -1;
                    }
                },
                new SignalMethod("getGsmSignalStrength") {
                    @Override
                    double getLevel(SignalStrength signalStrength) {
                        return (int) ((signalStrength.getGsmSignalStrength() / 31.0) * 4);
                    }
                },
                new SignalMethod("getCdmaDbm") {
                    @Override
                    double getLevel(SignalStrength signalStrength) {
                        return getDbmLevel(signalStrength.getCdmaDbm());
                    }
                },
                new SignalMethod("getCdmaEcio") {
                    @Override
                    double getLevel(SignalStrength signalStrength) {
                        return getEcioLevel(signalStrength.getCdmaEcio());
                    }
                },
                new SignalMethod("getEvdoDbm") {
                    @Override
                    double getLevel(SignalStrength signalStrength) {
                        return getDbmLevel(signalStrength.getEvdoDbm());
                    }
                },
                new SignalMethod("getEvdoEcio") {
                    @Override
                    double getLevel(SignalStrength signalStrength) {
                        return getEcioLevel(signalStrength.getEvdoEcio());
                    }
                },
                new SignalMethod("getEvdoSnr") {
                    @Override
                    double getLevel(SignalStrength signalStrength) {
                        return getSnrLevel(signalStrength.getEvdoSnr());
                    }
                },
                new SignalMethod("getAsuLevel") {
                    @Override
                    double getLevel(SignalStrength signalStrength) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
                        Method method = signalStrength.getClass().getDeclaredMethod("getAsuLevel");
                        method.setAccessible(true);
                        return getAsuLevel((int) method.invoke(signalStrength));
                    }
                },
                new SignalMethod("isGsm ? getGsm : getCdma") {
                    @Override
                    double getLevel(SignalStrength signalStrength) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
                        if (signalStrength.isGsm()) {
                            if (signalStrength.getGsmSignalStrength() != 99)
                                return getAsuLevel(signalStrength.getGsmSignalStrength());
                            else
                                return signalStrength.getGsmSignalStrength();
                        } else return getDbmLevel(signalStrength.getCdmaDbm());
                    }
                },
                new SignalMethod("evdoSnr : (cdmaDbm < cdmaEcio ? dbm : ecio)") {
                    @Override
                    double getLevel(SignalStrength signalStrength) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
                        if (signalStrength.getEvdoSnr() == -1) {
                            int levelDbm, levelEcio;
                            int cdmaDbm = signalStrength.getCdmaDbm();
                            int cdmaEcio = signalStrength.getCdmaEcio();

                            if (cdmaDbm >= -75) levelDbm = 4;
                            else if (cdmaDbm >= -85) levelDbm = 3;
                            else if (cdmaDbm >= -95) levelDbm = 2;
                            else if (cdmaDbm >= -100) levelDbm = 1;
                            else levelDbm = 0;

                            if (cdmaEcio >= -90) levelEcio = 4;
                            else if (cdmaEcio >= -110) levelEcio = 3;
                            else if (cdmaEcio >= -130) levelEcio = 2;
                            else if (cdmaEcio >= -150) levelEcio = 1;
                            else levelEcio = 0;

                            return (levelDbm < levelEcio) ? levelDbm : levelEcio;
                        } else return getSnrLevel(signalStrength.getEvdoSnr());
                    }
                },
                new SignalMethod("getAllCellInfo[0].getCellSignalStrength") {
                    @Override
                    double getLevel(SignalStrength signalStrength) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                            for (CellInfo info : telephonyManager.getAllCellInfo()) {
                                if (info.isRegistered()) {
                                    if (info instanceof CellInfoWcdma) {
                                        CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) telephonyManager.getAllCellInfo().get(0);
                                        CellSignalStrengthWcdma cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();
                                        return getDbmLevel(cellSignalStrengthWcdma.getDbm());
                                    } else if (info instanceof CellInfoGsm) {
                                        CellInfoGsm cellInfogsm = (CellInfoGsm) telephonyManager.getAllCellInfo().get(0);
                                        CellSignalStrengthGsm cellSignalStrengthGsm = cellInfogsm.getCellSignalStrength();
                                        return getDbmLevel(cellSignalStrengthGsm.getDbm());
                                    } else if (info instanceof CellInfoLte) {
                                        CellInfoLte cellInfoLte = (CellInfoLte) telephonyManager.getAllCellInfo().get(0);
                                        CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
                                        return getDbmLevel(cellSignalStrengthLte.getDbm());
                                    }
                                }
                            }
                        }

                        return -1;
                    }
                },
                new SignalMethod("getLteDbm (reflection)") {
                    @Override
                    double getLevel(SignalStrength signalStrength) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
                        Method method = signalStrength.getClass().getDeclaredMethod("getLteDbm");
                        method.setAccessible(true);
                        return getDbmLevel((int) method.invoke(signalStrength));
                    }
                },
                new SignalMethod("getLteLevel (reflection)") {
                    @Override
                    double getLevel(SignalStrength signalStrength) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
                        Method method = signalStrength.getClass().getDeclaredMethod("getLteLevel");
                        method.setAccessible(true);
                        return (int) method.invoke(signalStrength);
                    }
                },
                new SignalMethod("getLteSignalStrength (reflection)") {
                    @Override
                    double getLevel(SignalStrength signalStrength) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
                        Method method = signalStrength.getClass().getDeclaredMethod("getLteSignalStrength");
                        method.setAccessible(true);
                        return (int) method.invoke(signalStrength);
                    }

                    @Override
                    public boolean isExcluded() {
                        return true;
                    }
                },
                new SignalMethod("getLteRsrp (reflection)") {
                    @Override
                    double getLevel(SignalStrength signalStrength) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
                        Method method = signalStrength.getClass().getDeclaredMethod("getLteRsrp");
                        method.setAccessible(true);
                        return getRsrpLevel((int) method.invoke(signalStrength));
                    }
                },
                new SignalMethod("getLteRsrq (reflection)") {
                    @Override
                    double getLevel(SignalStrength signalStrength) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
                        Method method = signalStrength.getClass().getDeclaredMethod("getLteRsrq");
                        method.setAccessible(true);
                        return getRsrqLevel((int) method.invoke(signalStrength));
                    }

                    @Override
                    public boolean isExcluded() {
                        return true;
                    }
                },
                new SignalMethod("getLteRssnr (reflection)") {
                    @Override
                    double getLevel(SignalStrength signalStrength) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
                        Method method = signalStrength.getClass().getDeclaredMethod("getLteRssnr");
                        method.setAccessible(true);
                        return getSnrLevel((int) method.invoke(signalStrength));
                    }
                },
                new SignalMethod("getLteCqi (reflection)") {
                    @Override
                    double getLevel(SignalStrength signalStrength) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
                        Method method = signalStrength.getClass().getDeclaredMethod("getLteCqi");
                        method.setAccessible(true);
                        return (int) method.invoke(signalStrength);
                    }
                },
                new SignalMethod("getDbm (reflection)") {
                    @Override
                    double getLevel(SignalStrength signalStrength) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
                        Method method = signalStrength.getClass().getDeclaredMethod("getDbm");
                        method.setAccessible(true);
                        return getDbmLevel((int) method.invoke(signalStrength));
                    }
                },
                new SignalMethod("getGsmDbm (reflection)") {
                    @Override
                    double getLevel(SignalStrength signalStrength) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
                        Method method = signalStrength.getClass().getDeclaredMethod("getGsmDbm");
                        method.setAccessible(true);
                        return getDbmLevel((int) method.invoke(signalStrength));
                    }
                },
                new SignalMethod("getGsmLevel (reflection)") {
                    @Override
                    double getLevel(SignalStrength signalStrength) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
                        Method method = signalStrength.getClass().getDeclaredMethod("getGsmLevel");
                        method.setAccessible(true);
                        return (int) method.invoke(signalStrength);
                    }
                },
                new SignalMethod("getCdmaLevel (reflection)") {
                    @Override
                    double getLevel(SignalStrength signalStrength) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
                        Method method = signalStrength.getClass().getDeclaredMethod("getCdmaLevel");
                        method.setAccessible(true);
                        return (int) method.invoke(signalStrength);
                    }
                },
                new SignalMethod("getEvdoLevel (reflection)") {
                    @Override
                    double getLevel(SignalStrength signalStrength) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
                        Method method = signalStrength.getClass().getDeclaredMethod("getEvdoLevel");
                        method.setAccessible(true);
                        return (int) method.invoke(signalStrength);
                    }
                },
                new SignalMethod("getTdScdmaLevel (reflection)") {
                    @Override
                    double getLevel(SignalStrength signalStrength) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
                        Method method = signalStrength.getClass().getDeclaredMethod("getTdScdmaLevel");
                        method.setAccessible(true);
                        return (int) method.invoke(signalStrength);
                    }

                    @Override
                    public boolean isExcluded() {
                        return true;
                    }
                },
                new SignalMethod("getTdScdmaDbm (reflection)") {
                    @Override
                    double getLevel(SignalStrength signalStrength) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
                        Method method = signalStrength.getClass().getDeclaredMethod("getTdScdmaDbm");
                        method.setAccessible(true);
                        return getDbmLevel((int) method.invoke(signalStrength));
                    }

                    @Override
                    public boolean isExcluded() {
                        return true;
                    }
                }
        ));

        for (SignalMethod method : methods) {
            method.bindView(gridLayout);
        }

        telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        listener = new Listener();
        telephonyManager.listen(listener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class Listener extends PhoneStateListener {

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            for (SignalMethod method : methods) {
                method.update(signalStrength);
            }
        }
    }

    private int getDbmLevel(int dbm) {
        if (dbm < -100) return 0;
        else if (dbm < -95) return 1;
        else if (dbm < -85) return 2;
        else if (dbm < -75) return 3;
        else if (dbm != 0) return 4;
        else return -1;
    }

    private int getEcioLevel(int ecio) {
        if (ecio >= -90) return 4;
        else if (ecio >= -110) return 3;
        else if (ecio >= -130) return 2;
        else if (ecio >= -150) return 1;
        else return 0;
    }

    private int getSnrLevel(int snr) {
        return snr / 2;
    }

    private int getAsuLevel(int asu) {
        if (asu == 99) return -1;
        else return (2 * asu) - 113;
    }

    private int getRsrpLevel(int rsrp) {
        if (rsrp > -84) return 4;
        else if (rsrp > -102) return 3;
        else if (rsrp > -111) return 2;
        else if (rsrp > -112) return 1;
        else return 0;
    }

    private int getRsrqLevel(int rsrq) {
        if (rsrq > -5) return 4;
        else if (rsrq > -10) return 2;
        else return 1;
    }

    private static boolean isValidLevel(double level) {
        return level >= 0 && level <= 4;
    }

    private abstract static class SignalMethod {

        private String name;
        private TextView nameView, valueView;

        private double level;

        private SignalMethod(String name) {
            this.name = name;
        }

        private void update(SignalStrength signalStrength) {
            level = -1;
            try {
                level = getLevel(signalStrength);
            } catch (Exception ignored) {
            }

            if (valueView != null) {
                valueView.setText(String.valueOf(level));
                valueView.setTextColor(isExcluded() || isValidLevel(level) ? Color.BLACK : Color.RED);
            }
        }

        abstract double getLevel(SignalStrength signalStrength) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException;

        private void bindView(ViewGroup viewGroup) {
            Context context = viewGroup.getContext();
            nameView = new TextView(context);
            valueView = new TextView(context);

            nameView.setTextColor(Color.BLACK);
            nameView.setText(name);
            nameView.setPadding(10, 10, 10, 10);
            valueView.setPadding(10, 10, 10, 10);

            viewGroup.addView(nameView);
            viewGroup.addView(valueView);
        }

        public boolean isExcluded() {
            return false;
        }
    }
}
