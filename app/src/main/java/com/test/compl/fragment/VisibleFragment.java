package com.test.compl.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.test.compl.broadcast.NotificationReceiver;
import com.test.compl.photogalley.R;
import com.test.compl.service.PollService;

import org.greenrobot.eventbus.EventBus;

public class VisibleFragment extends Fragment {

    private NotificationReceiver mReceiver;
    private EventBus mEventBus;

    public static VisibleFragment newInstance(String param1, String param2) {
        VisibleFragment fragment = new VisibleFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

//        mReceiver = new
        mEventBus = EventBus.getDefault();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_visible, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        IntentFilter intentFilter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
        getActivity().registerReceiver(mOnShowNotification, intentFilter, PollService.PREM_PRIVATE, null);
//        mEventBus.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        getActivity().unregisterReceiver(mOnShowNotification);
//        mEventBus.unregister(this);
    }

    private BroadcastReceiver mOnShowNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            setResultCode(Activity.RESULT_CANCELED);
        }
    };

}
