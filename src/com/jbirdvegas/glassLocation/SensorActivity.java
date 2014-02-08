package com.jbirdvegas.glassLocation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by jbird on 2/8/14.
 */
public class SensorActivity extends BaseActivity {
    public static final String UPDATE_ACCELOROMETER = "sensor_update";
    private static final String ACCELEROMETER_UPDATE = "accelerometer";
    private static final String TAG = SensorActivity.class.getSimpleName();
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerReceivers();
        setupBase();
        setupViews(getIntent());

    }

    private void registerReceivers() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                checkForKillCommand(intent);
                Log.d(TAG, "Intent received! " + getDateFormated());
                mSecondsSince = 0;
                setupViews(intent);
                setContentView(mCard.toView());
            }
        };
        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(mBroadcastReceiver, new IntentFilter(SensorService.SENSOR_UPDATE));
    }

    private void setupViews(Intent intent) {

//        if (Sensor.TYPE_ACCELEROMETER.equals(intent.getStringExtra(SensorService.SENSOR_NAME))
        float values[] = intent.getFloatArrayExtra(SensorService.SENSOR_VALUES);
        if (values != null) {
            float x = values[0];
            float y = values[2];
            float z = values[1];
            int accuracy = intent.getIntExtra(SensorService.SENSOR_ACCURACY, -1);
            StringBuilder builder = new StringBuilder(0);
            builder.append("X: ").append(getFormatedAxisFloat(x)).append('\n');
            builder.append("Y: ").append(getFormatedAxisFloat(y)).append('\n');
            builder.append("Z: ").append(getFormatedAxisFloat(z)).append('\n');
            builder.append("Accuracy: ").append(accuracy);
            mCard.setText(builder.toString());
        }
    }

    private String getFormatedAxisFloat(Float value) {
        return String.format("%.3f", value);
    }
    @Override
    protected Intent getUpdateServiceIntent() {
        return new Intent(this, SensorService.class);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
