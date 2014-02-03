/*
 * HelloGlass.java
 * @author Cody Engel
 * http://codyengel.info
 */
package com.jbirdvegas.glassLocation;

// Glass Specific

import android.app.Service;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.TimelineManager;

public class HelloGlass extends Service implements LocationListener{
	
	private static final String LIVE_CARD_ID = "helloglass";
    public static final String MESSAGE = "message";
    public static final String LOCATION_MESSAGE = "location_message";
    public static final String STATUS_MESSAGE = "status_message";
    public static final String PROVIDER_MESSAGE = "provider_message";
    private static final String TAG = HelloGlass.class.getSimpleName();

    /*
     * TimelineManager allows applications to interact with the timeline.
     * 
     * Additional information: https://developers.google.com/glass/develop/gdk/reference/com/google/android/glass/timeline/TimelineManager
     */
	private TimelineManager mTimelineManager;
	
	/*
	 * LiveCard lets you create cards as well as publish them to the users timeline.
	 * 
	 * Additional information: https://developers.google.com/glass/develop/gdk/reference/com/google/android/glass/timeline/LiveCard
	 */
	@SuppressWarnings("unused")
	private LiveCard mLiveCard;
    private LocationManager mLocationManager;

    @Override
	public void onCreate() {
		super.onCreate();
		mTimelineManager = TimelineManager.from(this);
	} // onCreate

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	} // IBinder
	
	/*
	 * onStartCommand is used to start a service from your voice trigger you set up in res/xml/voice_trigger_start.xml
	 */
	public int onStartCommand(Intent intent, int flags, int startId) {
        location();
		// Where the magic happens
		mLiveCard = mTimelineManager.createLiveCard(LIVE_CARD_ID);
        mLiveCard.publish(LiveCard.PublishMode.REVEAL);
		Intent i = new Intent(this, getLiveCardClass());

        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Log.d(TAG, "Attempting to launch activity");
        sendMessage(getUpdateIntent());
        startActivity(i);

		return Service.START_REDELIVER_INTENT;
	} // onStartCommand

    @Override
    public boolean stopService(Intent name) {
        Log.d(TAG, "stopService called");
        return super.stopService(name);
    }

    private void location() {
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(true);

        String provider = mLocationManager.getBestProvider(criteria, true);
        boolean isEnabled = mLocationManager.isProviderEnabled(provider);
        if (isEnabled) {
            // Define a listener that responds to location updates
            LocationListener locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    // Called when a new location is found by the network location provider.
                    makeUseOfNewLocation(location);
                }

                public void onStatusChanged(String provider, int status, Bundle extras) {}

                public void onProviderEnabled(String provider) {}

                public void onProviderDisabled(String provider) {}
            };

            // Register the listener with the Location Manager to receive location updates
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, locationListener);
//        List<String> providers = locationManager.getProviders(
//                criteria, true /* enabledOnly */);
//
//        for (String provider : providers) {
//            long minTime = 1000;
//            float minDistance = 500;
//            Log.d(TAG, "found provider: " + provider);
//            locationManager.requestLocationUpdates(provider, minTime,
//                    minDistance, this);
//        }
        }
    }

    private void makeUseOfNewLocation(Location location) {
        Intent intent = getUpdateIntent()
                .putExtra(LOCATION_MESSAGE, location);
        Log.d(TAG, "location returned: " + location.toString());
//        startActivity(intent);
        String lat = "Latitude: " + location.getLatitude();
        String lng = ", Longitude: " + location.getLongitude();
        String summary = lat + lng;
        //TextToSpeech mSpeech = new TextToSpeech(this, null);
        //mSpeech.speak(summary, TextToSpeech.QUEUE_FLUSH, null);
        sendMessage(intent);
    }

    private Intent getUpdateIntent() {
        return new Intent(Magic.LOCATION_UPDATE);
    }

    private Class<?> getLiveCardClass() {
        return Magic.class;
    }

    @Override
    public void onLocationChanged(Location location) {
        makeUseOfNewLocation(location);
    }

    private void sendMessage(Intent intent) {
        LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(intent);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Intent intent = getUpdateIntent()
                .putExtra(STATUS_MESSAGE, s);
        debugBundle(bundle);
//        startActivity(intent);
        sendMessage(intent);

    }

    public void debugBundle(Bundle bundle) {
        StringBuilder builder = new StringBuilder(0);
        for (String key : bundle.keySet()) {
            builder.append(key);
            builder.append(':');
            builder.append(bundle.getString(key));
            builder.append('\n');
        }
        Log.d(TAG, "DEBUG_INTENT:" + builder.toString());
    }

    @Override
    public void onProviderEnabled(String s) {
        Intent intent = getUpdateIntent()
                .putExtra(PROVIDER_MESSAGE, s);
//        startActivity(intent);
        sendMessage(intent);
    }

    @Override
    public void onProviderDisabled(String s) {
        Intent intent = getUpdateIntent()
                .putExtra(PROVIDER_MESSAGE, s);
//        startActivity(intent);
        sendMessage(intent);
    }
}