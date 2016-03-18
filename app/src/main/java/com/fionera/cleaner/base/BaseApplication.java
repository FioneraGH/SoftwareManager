package com.fionera.cleaner.base;

import android.app.Application;
import android.content.Context;

public class BaseApplication extends Application {

    private static BaseApplication mInstance;

    public static BaseApplication getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }
}
