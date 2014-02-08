package com.jbirdvegas.glassLocation;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.TimelineManager;

/**
 * Created by jbird on 2/8/14.
 */
public class BaseService extends Service {
    private static final String LIVE_CARD_ID = "helloglass";
    protected TimelineManager mTimelineManager;
    /*
	 * LiveCard lets you create cards as well as publish them to the users timeline.
	 *
	 * Additional information: https://developers.google.com/glass/develop/gdk/reference/com/google/android/glass/timeline/LiveCard
	 */
    protected LiveCard mLiveCard;
    public static final String MESSAGE = "message";
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected void sendMessage(Intent intent) {
        LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(intent);
    }

    protected Class<?> getLiveCardClass() {
        return LocationActivity.class;
    }

    /**
     * Children should specify the update intent
     * @return intent used to update screen
     */
    protected Intent getUpdateIntent() {
        return new Intent(getApplicationContext(), LocationActivity.class);
    }

    protected void setupService() {
        mTimelineManager = TimelineManager.from(this);
        // Where the magic happens
        mLiveCard = mTimelineManager.createLiveCard(LIVE_CARD_ID);
        mLiveCard.publish(LiveCard.PublishMode.REVEAL);
    }
}
