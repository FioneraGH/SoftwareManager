package com.fionera.cleaner.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;

import com.fionera.cleaner.bean.AppProcessInfo;
import com.fionera.cleaner.R;
import com.fionera.cleaner.utils.LogCat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class CoreService
        extends Service {

    private OnProcessActionListener mOnActionListener;

    public void setOnActionListener(OnProcessActionListener listener) {
        mOnActionListener = listener;
    }

    private ActivityManager activityManager = null;
    private PackageManager packageManager = null;
    private Context mContext;

    public interface OnProcessActionListener {
        void onScanStarted(Context context);

        void onScanProgressUpdated(Context context, int current, int max);

        void onScanCompleted(Context context, List<AppProcessInfo> apps);

        void onCleanStarted(Context context);

        void onCleanCompleted(Context context, long cacheSize);
    }

    @Override
    public void onCreate() {
        mContext = getApplicationContext();
        try {
            activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            packageManager = mContext.getPackageManager();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    public class ProcessServiceBinder
            extends Binder {
        public CoreService getService() {
            return CoreService.this;
        }
    }

    private ProcessServiceBinder mBinder = new ProcessServiceBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    private class TaskScan
            extends AsyncTask<Void, Integer, List<AppProcessInfo>> {

        private int mAppCount = 0;

        @Override
        protected void onPreExecute() {
            if (mOnActionListener != null) {
                mOnActionListener.onScanStarted(CoreService.this);
            }
        }

        @Override
        protected List<AppProcessInfo> doInBackground(Void... params) {
            List<AppProcessInfo> appProcessInfoArrayList = new ArrayList<>();
            AppProcessInfo appProcessInfo;

            List<ActivityManager.RunningAppProcessInfo> appProcessList = activityManager
                    .getRunningAppProcesses();
            publishProgress(0, appProcessList.size());

            for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : appProcessList) {
                publishProgress(++mAppCount, appProcessList.size());
                appProcessInfo = new AppProcessInfo(runningAppProcessInfo.processName,
                                                    runningAppProcessInfo.pid,
                                                    runningAppProcessInfo.uid);
                try {
                    ApplicationInfo appInfo;
                    if (runningAppProcessInfo.processName.contains(":")) {
                        appInfo = getApplicationInfo(
                                runningAppProcessInfo.processName.split(":")[0]);
                    } else {
                        appInfo = packageManager
                                .getApplicationInfo(runningAppProcessInfo.processName, 0);
                    }
                    if (appInfo != null) {
                        appProcessInfo.isSystem = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM)
                                != 0;

                        appProcessInfo.icon = appInfo.loadIcon(packageManager);
                        appProcessInfo.appName = appInfo.loadLabel(packageManager).toString();
                    } else {
                        appProcessInfo.isSystem = true;
                        appProcessInfo.icon = ContextCompat
                                .getDrawable(mContext, R.drawable.ic_launcher);
                        appProcessInfo.appName = runningAppProcessInfo.processName;
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                appProcessInfo.memory = (long) (activityManager
                        .getProcessMemoryInfo(new int[]{runningAppProcessInfo.pid})[0]
                        .getTotalPrivateDirty() << 10);

                appProcessInfoArrayList.add(appProcessInfo);
            }
            return appProcessInfoArrayList;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (mOnActionListener != null) {
                mOnActionListener.onScanProgressUpdated(CoreService.this, values[0], values[1]);
            }
        }

        @Override
        protected void onPostExecute(List<AppProcessInfo> result) {
            if (mOnActionListener != null) {
                mOnActionListener.onScanCompleted(CoreService.this, result);
            }
        }
    }

    public void scanRunProcess() {
        new TaskScan().execute();
    }


    public void killBackgroundProcesses(String processName) {

        String packageName;
        try {
            if (!processName.contains(":")) {
                packageName = processName;
            } else {
                packageName = processName.split(":")[0];
            }

            activityManager.killBackgroundProcesses(packageName);

            Method forceStopPackage = activityManager.getClass()
                    .getDeclaredMethod("forceStopPackage", String.class);
            forceStopPackage.setAccessible(true);
            forceStopPackage.invoke(activityManager, packageName);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private class TaskClean
            extends AsyncTask<Void, Void, Long> {

        @Override
        protected void onPreExecute() {
            if (mOnActionListener != null) {
                mOnActionListener.onCleanStarted(CoreService.this);
            }
        }

        @Override
        protected Long doInBackground(Void... params) {
            long beforeMemory;
            long endMemory;
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memoryInfo);
            beforeMemory = memoryInfo.availMem;
            List<ActivityManager.RunningAppProcessInfo> appProcessList = activityManager
                    .getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo info : appProcessList) {
                killBackgroundProcesses(info.processName);
            }
            activityManager.getMemoryInfo(memoryInfo);
            endMemory = memoryInfo.availMem;
            return endMemory - beforeMemory;
        }

        @Override
        protected void onPostExecute(Long result) {
            if (mOnActionListener != null) {
                mOnActionListener.onCleanCompleted(CoreService.this, result);
            }
        }
    }

    public void cleanAllProcess() {
        new TaskClean().execute();
    }

    public ApplicationInfo getApplicationInfo(String processName) {
        if (processName == null) {
            return null;
        }
        List<ApplicationInfo> appList = packageManager
                .getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo appInfo : appList) {
            if (processName.equals(appInfo.processName)) {
                return appInfo;
            }
        }
        return null;
    }
}
