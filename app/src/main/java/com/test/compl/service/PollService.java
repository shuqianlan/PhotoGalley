package com.test.compl.service;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.test.compl.BaseActivity;
import com.test.compl.Utils;
import com.test.compl.photogalley.PhotoGalleyActivity;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class PollService extends IntentService {

    public static final String TAG = "PollService";
    public static final String ACTION_SHOW_NOTIFICATION = "com.test.compl.service.photo.SHOW_NOTIFICATION";
    public static final String PREM_PRIVATE = "com.test.compl.photogalley.PhotoGalleyActivity.PRIVATE";
    public static final String REQUEST_CODE = "REQUEST_CODE";
    public static final String NOTIFICATION = "NOTIFICATION";

    private static final long POLL_INTEWRVAL_MS = TimeUnit.MINUTES.toMillis(1);
    private AtomicInteger count = new AtomicInteger(0);

    public PollService() {
        super(TAG);
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, PollService.class);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent: " + intent);

        if (!isNetworkAvailableAndConnected()) {
            return;
        }

        Intent intent1 = BaseActivity.newIntent(this);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 2, intent1, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(new NotificationChannel("notice", "notice", NotificationManager.IMPORTANCE_MIN));
            Notification notification = new Notification.Builder(this, "notice")
                    .setTicker("Notice" + count.incrementAndGet())
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentText("hello, Java")
                    .setContentTitle("Title")
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .build();

            showBackgroundNotification(0, notification);
        }

//        sendBroadcast(new Intent(ACTION_SHOW_NOTIFICATION), PREM_PRIVATE); // 权限校验.
    }

    private void showBackgroundNotification(int requestCode, Notification notification) {
        Intent i = new Intent(ACTION_SHOW_NOTIFICATION);
        i.putExtra(REQUEST_CODE, requestCode);
        i.putExtra(NOTIFICATION, notification);
        sendOrderedBroadcast(i, PREM_PRIVATE, null, null, Activity.RESULT_OK, null, null);
    }

    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean isNetworkAvailable = manager.getActiveNetworkInfo() != null;
        return isNetworkAvailable && manager.getActiveNetworkInfo().isConnected();
    }

    public static void setServiceAlarm(Context context, boolean isON) {
        Intent intent = newIntent(context);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (isON) {
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), POLL_INTEWRVAL_MS, pendingIntent);
        } else {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }

        Utils.setAlarmOn(isON);
    }

    public static boolean isServiceAlarmOn(Context context) {
        Intent intent = newIntent(context);

        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_NO_CREATE);
        return pendingIntent != null;
    }
}
