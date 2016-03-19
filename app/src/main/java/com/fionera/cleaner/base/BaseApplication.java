package com.fionera.cleaner.base;

import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;

public class BaseApplication extends Application {

    private static BaseApplication mInstance;

    public static int screenWidth;
    public static int screenHeight;
    public static float screenDensity;
    public static float scaledDensity;

    public static BaseApplication getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        getDisplayMetrics();
    }

    private void getDisplayMetrics() {

        DisplayMetrics metric = getApplicationContext().getResources().getDisplayMetrics();
        screenWidth = metric.widthPixels;
        screenHeight = metric.heightPixels;
        screenDensity = metric.density;
        scaledDensity = metric.scaledDensity;
    }
}
