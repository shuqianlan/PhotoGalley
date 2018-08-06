package com.test.compl;

import android.preference.PreferenceManager;

public class Utils {

    public static final String PREF_IS_ALARM_ON = "isAlarmOn";

    public static boolean isAlarmOn() {
        return PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).getBoolean(PREF_IS_ALARM_ON, false);
    }

    public static void setAlarmOn(boolean isON) {
        PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext())
                .edit()
                .putBoolean(PREF_IS_ALARM_ON, isON)
                .commit();
    }
}
