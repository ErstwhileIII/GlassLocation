package com.jbirdvegas.glassLocation;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.WindowManager;
import com.google.android.glass.app.Card;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by jbird on 2/8/14.
 */
public abstract class BaseActivity extends Activity {
    protected static final long DELAY = 5000;
    private static final String TAG = BaseActivity.class.getSimpleName();
    protected Card mCard;
    protected Timer mTimer;
    protected int mSecondsSince = 0;
    private CounterTask mCounterTask;
    protected List<PendingIntent> mAlarms = new ArrayList<PendingIntent>(0);

    protected void setAlarm() {
        Intent intent = getUpdateServiceIntent();
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent service = PendingIntent.getService(this, 42, intent, 0);
        mAlarms.add(service);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + DELAY, service);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            openOptionsMenu();
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d(TAG, "onBackButton...");
            cleanUp();
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void cleanUp() {
        resetTimer();
        startService(cancelAll());
    }

    @Override
    protected void onStop() {
        super.onStop();
        cleanUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cleanUp();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.set_home_location:
                PrefsHelper.setHome(getApplicationContext());
                return true;
            case R.id.launch_sensors:
                cleanUp();
                Intent intent = new Intent(getApplicationContext(), SensorActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void resetTimer() {
        mTimer.cancel();
        mSecondsSince = 0;
    }

    protected Intent cancelAll() {
        mTimer.cancel();
        Intent intent = getUpdateServiceIntent();
        intent.putExtra(SensorService.KILL_UPDATES, true);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Log.d(TAG, String.format("Canceling %d alarms", mAlarms.size()));
        for (PendingIntent pendingIntent : mAlarms) {
            alarmManager.cancel(pendingIntent);
        }
        return intent;
    }

    protected String getDateFormated() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        return dateFormat.format(new Date());
    }

    protected abstract Intent getUpdateServiceIntent();

    protected void setupBase() {
        if (mTimer == null) {
            mTimer = new Timer();
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mCard = new Card(this);
        mCounterTask = new CounterTask();
        mTimer.schedule(mCounterTask, 1000, 1000);
    }

    protected void checkForKillCommand(Intent intent) {
        if (intent.getExtras() != null && intent.getExtras().getBoolean(SensorService.KILL_UPDATES)) {
            finish();
        }
    }

    private class CounterTask extends TimerTask {
        @Override
        public void run() {
            Log.d(TAG, "Timer fired: " + mSecondsSince);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mCard.setFootnote("Since last update: " + mSecondsSince++);
                    // its only null if child forgot to call setupBase()
                    if (mCard != null) {
                        setContentView(mCard.toView());
                    }
                }
            });
        }
    }
}
