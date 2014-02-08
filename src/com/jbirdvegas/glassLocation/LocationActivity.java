/*
 * Magic.java
 * @author Cody Engel
 * http://codyengel.info
 *
 * This is the service which is started from HelloGlass.java, this is where the magic happens.
 */
package com.jbirdvegas.glassLocation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import com.google.android.glass.app.Card;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class LocationActivity extends BaseActivity {

    private static final String TAG = LocationActivity.class.getSimpleName();
    public static final String LOCATION_UPDATE = "message_update_location";
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "LocationActivity onCreate");
        setupBase();
        checkForKillCommand(getUpdateServiceIntent());
		registerReceivers();
		/*
		 * We're creating a card for the interface.
		 *
		 * More info here: http://developer.android.com/guide/topics/ui/themes.html
		 */
        mCard = new Card(this);
//        mCard.setText("Testing");
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
                String provider = extras.getString(SensorService.PROVIDER_MESSAGE);
                Location location = extras.getParcelable(LocationService.LOCATION_MESSAGE);
                String status = extras.getString(SensorService.STATUS_MESSAGE);
                String emptyUpdate = extras.getString(LocationService.SEARCHING_METHOD);
                if (provider != null) {
                    Log.d(TAG, "provider: " + provider);
                    mCard.setText(provider);
                    mCard.setFootnote(R.string.provider_message);
                }
                if (location != null) {
                    storeLatestLocation(location);
                    displayLocationData(location);
                    mSecondsSince = -0;
                }
                if (status != null) {
                    Log.d(TAG, "status: " + status);
                    mCard.setText(status);
                    mCard.setFootnote(getString(R.string.status_message));
                }
                if (emptyUpdate != null) {
                    Log.d(TAG, "emptyUpdate: " + emptyUpdate);
//                    mCard.setFootnote(String.format("Since last update %s", convert(getDistanceToHome())));
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
        String format = String.format("Distance to home: %s",
                convert(home.distanceTo(location)));
        mCard.setText(locationText + '\n' + format);
        mCard.setFootnote(format);
    }

    private static String convert(float meters) {
        double miles = meters * 0.00062137119;
        DecimalFormat format = new DecimalFormat("#.##");
        if (miles < 1) {
            return format.format(meters) + " meters";
        } else {
            return format.format(miles) + " miles";
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
        // nothing yet
    }

    @Override
    protected Intent getUpdateServiceIntent() {
        return new Intent(this, SensorService.class);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "LocationActivity onStop");
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mBroadcastReceiver);
    }

    protected void onDestroy() {
        Log.d(TAG, "LocationActivity onDestroy");
        super.onDestroy();
    }
    private void registerReceivers() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                checkForKillCommand(intent);
                Log.d(TAG, "Intent received! " + getDateFormated());
                setViews(intent);
                setContentView(mCard.toView());
            }
        };
        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(mBroadcastReceiver, new IntentFilter(LOCATION_UPDATE));
    }

    private String getLocationText(Location location) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        List<Address> address = null;
        try {
            address = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            Log.d(TAG, address.get(0).getAddressLine(0));
        } catch (IOException e) {
            return "Failed to find address";
        }
        return String.format("lat:%s\nlong:%s\naccuracy:%s\ntime:%s\n%s",
                location.getLatitude(), location.getLongitude(),
                location.getAccuracy(), getDateFormated(),
                address.get(0).getAddressLine(0) + "\n" + address.get(0).getAddressLine(1));
    }

}