/*
 * Magic.java
 * @author Cody Engel
 * http://codyengel.info
 *
 * This is the service which is started from HelloGlass.java, this is where the magic happens.
 */
package com.jbirdvegas.glassLocation;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import com.google.android.glass.app.Card;

import java.util.Date;

public class Magic extends Activity {

    private static final String TAG = Magic.class.getSimpleName();
    public static final String LOCATION_UPDATE = "message_update_location";
    private static final long TWO_SECONDS = 5000;
    private Card mCard;
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Magic onCreate");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		registerReceivers();
		/*
		 * We're creating a card for the interface.
		 *
		 * More info here: http://developer.android.com/guide/topics/ui/themes.html
		 */
        mCard = new Card(this);
        mCard.setText("Testing");
        Intent intent = getIntent();
        setViews(intent);
        View card1View = mCard.toView();

        // Display the card we just created
        setContentView(card1View);
    }

    private void setViews(Intent intent) {
        Log.d(TAG, "Setting views");
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                String provider = extras.getString(HelloGlass.PROVIDER_MESSAGE);
                Location location = extras.getParcelable(HelloGlass.LOCATION_MESSAGE);
                String status = extras.getString(HelloGlass.STATUS_MESSAGE);
                if (provider != null) {
                    Log.d(TAG, "provider: " + provider);
                    mCard.setText(provider);
                    mCard.setFootnote(R.string.provider_message);
                }
                if (location != null) {
                    Location home = new Location("Home");
                    home.setLatitude(36.1591884);
                    home.setLongitude(-86.8691655);
                    Log.d(TAG, "location: " + getLocationText(location));
                    mCard.setText(getLocationText(location));
                    mCard.setFootnote(String.format("Distance to home: %s meters", home.distanceTo(location)));
                }
                if (status != null) {
                    Log.d(TAG, "status: " + status);
                    mCard.setText(status);
                    mCard.setFootnote(getString(R.string.status_message));
                }
            } else {
                mCard.setText("No information yet ~onCreate");
                mCard.setFootnote("waiting... " + System.currentTimeMillis());

            }
            setAlarm();
        } else {
            mCard.setText("Hello, Sir!"); // Main text area
            mCard.setFootnote(String.valueOf(System.currentTimeMillis())); // Footer
        }
    }

    private void setAlarm() {
        Intent intent = new Intent(Magic.this, HelloGlass.class);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent service = PendingIntent.getService(this, 42, intent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + TWO_SECONDS, service);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setViews(null);
        Log.d(TAG, "Magic onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "Magic onStop");
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mBroadcastReceiver);
    }

    private void registerReceivers() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "Intent received! " + System.currentTimeMillis());
                mCard.setText("Received intent: " + new Date().toString());
                setViews(intent);
                setContentView(mCard.toView());
            }
        };
        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(mBroadcastReceiver, new IntentFilter(LOCATION_UPDATE));
    }

    private String getLocationText(Location location) {
        return String.format("lat:%s\nlong:%s\naccuracy:%s\naltitude:%s\ntime:%s",
                location.getLatitude(), location.getLongitude(),
                location.getAccuracy(), location.getAltitude(),
                new Date().toString());
    }
}