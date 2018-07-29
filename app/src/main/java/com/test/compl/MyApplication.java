package com.test.compl;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {

    public static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();

        sContext = getApplicationContext();
    }

    public static Context getContext() {
        return sContext;
    }
}
