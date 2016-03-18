package com.fionera.cleaner.utils;

import android.widget.Toast;

import com.fionera.cleaner.base.BaseApplication;

/**
 * Created by fionera on 15-12-6.
 */
public class ShowToast {

    private static Toast toast;

    public static void show(Object info) {

        if (toast == null) {
            toast = Toast
                    .makeText(BaseApplication.getInstance(), info.toString(), Toast.LENGTH_SHORT);
        } else {
            toast.setText(info.toString());
        }
        toast.show();
    }
}
