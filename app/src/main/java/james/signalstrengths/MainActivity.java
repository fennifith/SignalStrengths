package james.signalstrengths;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.widget.GridLayout;
import android.widget.TextView;

import james.signalstrengthslib.SignalMethod;
import james.signalstrengthslib.SignalStrengths;
import james.signalstrengthslib.SignalUtils;

public class MainActivity extends AppCompatActivity {

    private GridLayout gridLayout;
    private TextView firstValid, average;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gridLayout = (GridLayout) findViewById(R.id.gridLayout);
        firstValid = (TextView) findViewById(R.id.firstValid);
        average = (TextView) findViewById(R.id.average);

        for (SignalMethod method : SignalStrengths.getMethods()) {
            TextView nameView = new TextView(this);
            nameView.setPadding(10, 10, 10, 10);
            nameView.setText(method.getName());

            TextView valueView = new TextView(this);
            valueView.setPadding(10, 10, 10, 10);
            valueView.setTag(method.getName());

            gridLayout.addView(nameView);
            gridLayout.addView(valueView);
        }

        ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).listen(new PhoneStateListener() {
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);

                firstValid.setText(String.valueOf(SignalStrengths.getFirstValid(signalStrength)));
                average.setText(String.valueOf(SignalStrengths.getAverage(signalStrength)));

                for (SignalMethod method : SignalStrengths.getMethods()) {
                    double level = 0;
                    try {
                        level = method.getLevel(signalStrength);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    TextView valueView = (TextView) gridLayout.findViewWithTag(method.getName());
                    valueView.setTextColor(method.isExcluded() ? Color.GRAY : (SignalUtils.isValidLevel(level) ? Color.BLACK : Color.RED));
                    valueView.setText(String.valueOf(level));
                }
            }
        }, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }
}
