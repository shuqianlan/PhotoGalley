package com.test.compl;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

public class MyApplication extends Application {

    public static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();

        sContext = getApplicationContext();
//        StrictMode.enableDefaults();
    }

    public static Context getContext() {
        return sContext;
    }
}
