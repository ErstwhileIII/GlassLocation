/*
 * HelloGlass.java
 * @author Cody Engel
 * http://codyengel.info
 */
package com.jbirdvegas.glassLocation;

// Glass Specific

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

public class SensorService extends BaseService implements SensorEventListener{
    public static final String SENSOR_UPDATE = "sensor_update";
    public static final String STATUS_MESSAGE = "status_message";
    public static final String PROVIDER_MESSAGE = "provider_message";
    private static final String TAG = SensorService.class.getSimpleName();
    public static final String KILL_UPDATES = "kill_updates";
    public static final String SENSOR_VALUES = "sensor_values";
    public static final String SENSOR_NAME = "sensor_name";
    public static final String SENSOR_ACCURACY = "sensor_accuracy";
    private static final String SENSOR_TIMESTAMP = "sensor_timestamp";
    private static final String SENSOR_VENDER = "sensor_vendor";
    private SensorManager mSensorManager;
    private Sensor mAccellerometer;
    private Sensor mOrientation;

    @Override
    public void onCreate() {
        super.onCreate();
    } // onCreate

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return null;
    } // IBinder

    private static boolean isStarted = false;
    /*
     * onStartCommand is used to start a service from your voice trigger you set up in res/xml/voice_trigger_location.xmlxml
     */
    public int onStartCommand(Intent intent, int flags, int startId) {
        setupService();
        if (!isStarted) {
            initSensorListeners();
            isStarted = true;
        }
        if (intent != null && intent.getExtras() != null) {
            if (intent.getExtras().getBoolean(KILL_UPDATES, false)) {
                killAllLocationThings(intent);
                return START_NOT_STICKY;
            }
        }

        Intent i = new Intent(this, getLiveCardClass());

        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Log.d(TAG, "Attempting to launch Sensoractivity");
        startActivity(i);

        return Service.START_NOT_STICKY;
    } // onStartCommand

    @Override
    protected Class<?> getLiveCardClass() {
        return SensorActivity.class;
    }

    private void killAllLocationThings(Intent intent) {
        if (mSensorManager != null && mSensorManager != null) {
            mSensorManager.unregisterListener(this);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            PendingIntent service = PendingIntent.getService(this, 42, intent, 0);
            alarmManager.cancel(service);
        }
        stopSelf();
    }

    @Override
    public boolean stopService(Intent name) {
        Log.d(TAG, "stopService called");
        mSensorManager.unregisterListener(this);
        return super.stopService(name);
    }

    private void initSensorListeners() {
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccellerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mSensorManager.registerListener(this, mOrientation, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mAccellerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void sensorUpdate(SensorEvent event) {
        Intent intent = getUpdateIntent()
                .putExtra(SENSOR_VALUES, event.values);
        intent.putExtra(SENSOR_NAME, event.sensor.getName());
        intent.putExtra(SENSOR_ACCURACY, event.accuracy);
        intent.putExtra(SENSOR_TIMESTAMP, event.timestamp);
        intent.putExtra(SENSOR_VENDER, event.sensor.getVendor());
        sendMessage(intent);
    }

    @Override
    protected Intent getUpdateIntent() {
        return new Intent(SENSOR_UPDATE);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Log.d(TAG, "SensorEvent for: " + sensorEvent.sensor.getName());
        sensorUpdate(sensorEvent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.d(TAG, "Accuracy changed on sensor: " + sensor.getName() + " to " + i);
    }
}