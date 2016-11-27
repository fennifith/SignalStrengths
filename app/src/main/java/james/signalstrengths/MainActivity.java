package james.signalstrengths;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.CellInfoGsm;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.widget.TextView;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    private TelephonyManager telephonyManager;
    private Listener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

            ((TextView) findViewById(R.id.gsm)).setText(String.valueOf((int) ((signalStrength.getGsmSignalStrength() / 31.0) * 4)));

            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ((TextView) findViewById(R.id.level)).setText(String.valueOf(signalStrength.getLevel()));
            else ((TextView) findViewById(R.id.level)).setText("-1");

            ((TextView) findViewById(R.id.cdma)).setText(String.valueOf(getSignalStrengthDbm(signalStrength.getCdmaDbm())));
            ((TextView) findViewById(R.id.cdmaEcio)).setText(String.valueOf(getSignalStrengthEcio(signalStrength.getCdmaEcio())));

            ((TextView) findViewById(R.id.evdoDbm)).setText(String.valueOf(getSignalStrengthDbm(signalStrength.getEvdoDbm())));
            ((TextView) findViewById(R.id.evdoEcio)).setText(String.valueOf(getSignalStrengthEcio(signalStrength.getEvdoEcio())));
            ((TextView) findViewById(R.id.evdoSnr)).setText(String.valueOf(signalStrength.getEvdoSnr() / 2));

            ((TextView) findViewById(R.id.asu)).setText(String.valueOf(getSignalStrengthAsu(signalStrength)));

            ((TextView) findViewById(R.id.one)).setText(String.valueOf(getSignalStrengthEcio(getSignalStrength(signalStrength))));
            ((TextView) findViewById(R.id.two)).setText(String.valueOf(getSignalStrength2(signalStrength)));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                ((TextView) findViewById(R.id.three)).setText(String.valueOf(getSignalStrength3()));
            else ((TextView) findViewById(R.id.three)).setText("-1");
        }

        private int getSignalStrengthDbm(int dbm) {
            if (dbm < -100) return 0;
            else if (dbm < -95) return 1;
            else if (dbm < -85) return 2;
            else if (dbm < -75) return 3;
            else if (dbm != 0) return 4;
            else return -1;
        }

        private int getSignalStrengthEcio(int ecio) {
            if (ecio >= -90) return 4;
            else if (ecio >= -110) return 3;
            else if (ecio >= -130) return 2;
            else if (ecio >= -150) return 1;
            else return 0;
        }

        private int getSignalStrengthAsu(SignalStrength signalStrength) {
            try {
                Method method = signalStrength.getClass().getDeclaredMethod("getAsuLevel");
                method.setAccessible(true);
                return (int) method.invoke(signalStrength);
            } catch (Exception e) {
                return -1;
            }
        }

        private int getSignalStrength(SignalStrength signalStrength) {
            if (signalStrength.isGsm()) {
                if (signalStrength.getGsmSignalStrength() != 99)
                    return signalStrength.getGsmSignalStrength() * 2 - 113;
                else
                    return signalStrength.getGsmSignalStrength();
            } else return signalStrength.getCdmaDbm();
        }

        private int getSignalStrength2(SignalStrength signalStrength) {
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
            } else return signalStrength.getEvdoSnr() / 2;
        }

        @TargetApi(17)
        private int getSignalStrength3() {
            try {
                CellInfoGsm cellinfogsm = (CellInfoGsm) telephonyManager.getAllCellInfo().get(0);
                CellSignalStrengthGsm cellSignalStrengthGsm = cellinfogsm.getCellSignalStrength();
                return getSignalStrengthDbm(cellSignalStrengthGsm.getDbm());
            } catch (Exception e) {
                return -1;
            }
        }
    }
}
