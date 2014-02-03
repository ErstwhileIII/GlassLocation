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
import android.view.*;
import com.google.android.glass.app.Card;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
                String emptyUpdate = extras.getString(HelloGlass.SEARCHING_METHOD);
                if (provider != null) {
                    Log.d(TAG, "provider: " + provider);
                    mCard.setText(provider);
                    mCard.setFootnote(R.string.provider_message);
                }
                if (location != null) {
                    storeLatestLocation(location);
                    displayLocationData(location);
                }
                if (status != null) {
                    Log.d(TAG, "status: " + status);
                    mCard.setText(status);
                    mCard.setFootnote(getString(R.string.status_message));
                }
                if (emptyUpdate != null) {
                    Log.d(TAG, "emptyUpdate: " + emptyUpdate);
                    mCard.setFootnote(String.format("Distance to home %f", getDistanceToHome()));
                }
            } else {
                mCard.setText("No information yet ~onCreate");
                mCard.setFootnote("waiting... " + getDateFormated());

            }
            setAlarm();
        } else {
            mCard.setText("No information received... please wait"); // Main text area
            mCard.setFootnote(String.valueOf(System.currentTimeMillis())); // Footer
        }
    }

    private float getDistanceToHome() {
        return getHomeLocation().distanceTo(getMostRecentLocation());
    }

    private void displayLocationData(Location location) {
        Location home = getHomeLocation();
        String locationText = getLocationText(location);
        Log.d(TAG, "location: " + locationText);
        String format = String.format("Distance to home: %s meters",
                home.distanceTo(location));
        mCard.setText(locationText + '\n' + format);
        mCard.setFootnote(format);
    }

    private Location getHomeLocation() {
        Location home = new Location("Home");
        double latitude = 36.1591884;
        double longitude = -86.8691655;
        double storedHomeLat = PrefsHelper.getHomeLat(this);
        double storedHomeLong = PrefsHelper.getHomeLong(this);
        if (storedHomeLat != -1 && storedHomeLong != -1) {
            home.setLatitude(latitude);
            home.setLongitude(longitude);
        }
        return home;
    }

    private Location getMostRecentLocation() {
        Location recent = new Location("MostRecent");
        recent.setLatitude(PrefsHelper.getLastLat(this));
        recent.setLongitude(PrefsHelper.getLastLong(this));
        return recent;
    }

    private void storeLatestLocation(Location location) {
        PrefsHelper.setLastLocation(this, location.getLatitude(), location.getLongitude(), location.getAccuracy());
    }

    private String getDateFormated() {
        return new Date().toString();
    }

    private void setAlarm() {
        Intent intent = getUpdateServiceIntent();
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent service = PendingIntent.getService(this, 42, intent, 0);
        mAlarms.add(service);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + TWO_SECONDS, service);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            openOptionsMenu();
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            Intent intent = getUpdateServiceIntent();
            intent.putExtra(HelloGlass.KILL_UPDATES, true);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            for (PendingIntent pendingIntent : mAlarms) {
                alarmManager.cancel(pendingIntent);
            }
            startService(intent);
        }

        return super.onKeyDown(keyCode, event);

    }

    List<PendingIntent> mAlarms = new ArrayList<PendingIntent>(0);
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.magic, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
        // nothing yet
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                PrefsHelper.setHome(getApplicationContext());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Intent getUpdateServiceIntent() {
        return new Intent(this, HelloGlass.class);
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

    protected void onDestroy() {
        Log.d(TAG, "Magic onDestroy");
        super.onDestroy();
    }
    private void registerReceivers() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "Intent received! " + getDateFormated());
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
                getDateFormated());
    }
}