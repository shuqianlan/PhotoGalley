package com.test.compl.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.test.compl.MyApplication;
import com.test.compl.Utils;
import com.test.compl.service.PollService;

public class BootCompleteReceiver extends BroadcastReceiver {

    public static final String TAG = BootCompleteReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        //throw new UnsupportedOperationException("Not yet implemented");
//        Log.d(TAG, "onReceive() called. ");
//        Intent pollservice = PollService.newIntent(context);
//        context.startService(pollservice);
//        Toast.makeText(context, "Boot_Completed", Toast.LENGTH_SHORT).show();

        boolean isAlarmOn = Utils.isAlarmOn();
        PollService.setServiceAlarm(MyApplication.getContext(), isAlarmOn);
    }
}
