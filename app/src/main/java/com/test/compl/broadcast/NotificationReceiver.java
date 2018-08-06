package com.test.compl.broadcast;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.test.compl.service.PollService;

public class NotificationReceiver extends BroadcastReceiver {

    public static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (getResultCode() != Activity.RESULT_OK) {
            return;
        }

        int requestCode = intent.getIntExtra(PollService.REQUEST_CODE, 0);
        Notification notification = (Notification) intent.getSerializableExtra(PollService.NOTIFICATION);
        NotificationManagerCompat.from(context).notify(requestCode, notification);

//      Keep in mind that the work you do here will block further broadcasts until
//      it completes, so taking advantage of this at all excessively can be counter-productive
//      and cause later events to be received more slowly.
        PendingResult result = goAsync();
    }

}
