package com.fionera.cleaner.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.StatFs;
import android.support.v4.content.ContextCompat;

import com.fionera.cleaner.bean.CacheInfo;
import com.fionera.cleaner.utils.AppUtil;
import com.fionera.cleaner.utils.LogCat;
import com.fionera.cleaner.utils.ShowToast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class CleanerService
        extends Service {

    private Method mGetPackageSizeInfoMethod, mFreeStorageAndNotifyMethod;

    private OnActionListener mOnActionListener;
    private boolean mIsScanning = false;
    private boolean mIsCleaning = false;
    private long mCacheSize = 0;
    private Context mContext;

    public void setOnActionListener(OnActionListener listener) {
        mOnActionListener = listener;
    }

    public boolean isScanning() {
        return mIsScanning;
    }

    public boolean isCleaning() {
        return mIsCleaning;
    }

    public long getCacheSize() {
        return mCacheSize;
    }

    public interface OnActionListener {
        void onScanStarted(Context context);

        void onScanProgressUpdated(Context context, int current, int max);

        void onScanCompleted(Context context, List<CacheInfo> apps);

        void onCleanStarted(Context context);

        void onCleanCompleted(Context context, long cacheSize);
    }

    public class CleanerServiceBinder
            extends Binder {

        public CleanerService getService() {
            return CleanerService.this;
        }
    }

    private CleanerServiceBinder mBinder = new CleanerServiceBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        mContext = getApplicationContext();
        try {
            /**
             * PackageManager.class 无效
             */
            mGetPackageSizeInfoMethod = getPackageManager().getClass()
                    .getMethod("getPackageSizeInfo", String.class, IPackageStatsObserver.class);
            mFreeStorageAndNotifyMethod = getPackageManager().getClass()
                    .getMethod("freeStorageAndNotify", long.class, IPackageDataObserver.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    private class TaskScan
            extends AsyncTask<Void, Integer, List<CacheInfo>> {

        private int mAppCount = 0;

        @Override
        protected void onPreExecute() {
            if (mOnActionListener != null) {
                mOnActionListener.onScanStarted(CleanerService.this);
            }
        }

        @Override
        protected List<CacheInfo> doInBackground(Void... params) {
            mCacheSize = 0;
            final List<CacheInfo> cacheInfoList = new ArrayList<>();

            final PackageManager pm = getPackageManager();
            final List<ApplicationInfo> packages = pm
                    .getInstalledApplications(PackageManager.GET_META_DATA);
            final CountDownLatch countDownLatch = new CountDownLatch(packages.size());
            publishProgress(0, packages.size());
            try {
                for (ApplicationInfo pkg : packages) {
                    mGetPackageSizeInfoMethod
                            .invoke(pm, pkg.packageName, new IPackageStatsObserver.Stub() {

                                @Override
                                public void onGetStatsCompleted(PackageStats pStats,
                                                                boolean succeeded) throws
                                        RemoteException {
                                    synchronized (cacheInfoList) {
                                        publishProgress(++mAppCount, packages.size());
                                        if (succeeded && pStats.cacheSize > 0) {
                                            try {
                                                cacheInfoList.add(new CacheInfo(pStats.packageName,
                                                                                pm.getApplicationLabel(
                                                                                        pm.getApplicationInfo(
                                                                                                pStats.packageName,
                                                                                                0))
                                                                                        .toString(),
                                                                                pm.getApplicationIcon(
                                                                                        pStats.packageName),
                                                                                pStats.cacheSize));
                                                mCacheSize += pStats.cacheSize + pStats
                                                        .externalCacheSize;
                                            } catch (PackageManager.NameNotFoundException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }

                                    synchronized (countDownLatch) {
                                        countDownLatch.countDown();
                                    }
                                }
                            });
                }
                countDownLatch.await();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return cacheInfoList;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (mOnActionListener != null) {
                mOnActionListener.onScanProgressUpdated(CleanerService.this, values[0], values[1]);
            }
        }

        @Override
        protected void onPostExecute(List<CacheInfo> result) {
            if (mOnActionListener != null) {
                mOnActionListener.onScanCompleted(CleanerService.this, result);
            }
            mIsScanning = false;
        }
    }

    public void scanCache() {
        mIsScanning = true;
        new TaskScan().execute();
    }

    private class TaskClean
            extends AsyncTask<Void, Void, Long> {

        @Override
        protected void onPreExecute() {
            if (mOnActionListener != null) {
                mOnActionListener.onCleanStarted(CleanerService.this);
            }
        }

        @Override
        protected Long doInBackground(Void... params) {
            final CountDownLatch countDownLatch = new CountDownLatch(1);


            StatFs statFs = new StatFs(Environment.getDataDirectory().getAbsolutePath());
            try {
                mFreeStorageAndNotifyMethod.invoke(getPackageManager(),
                                                   (long) statFs.getBlockCount() * (long) statFs
                                                           .getBlockSize(),
                                                   new IPackageDataObserver.Stub() {
                                                       @Override
                                                       public void onRemoveCompleted(
                                                               String packageName,
                                                               boolean succeeded) throws
                                                               RemoteException {
                                                           countDownLatch.countDown();
                                                       }
                                                   });

                // TODO ROOT ABOVE M
                if (false) {
                    AppUtil.getRootPermission(mContext);
                }
                countDownLatch.await();
            } catch (InvocationTargetException | InterruptedException | IllegalAccessException e) {
                e.printStackTrace();
            }

            return mCacheSize;
        }

        @Override
        protected void onPostExecute(Long result) {
            mCacheSize = 0;

            if (mOnActionListener != null) {
                mOnActionListener.onCleanCompleted(CleanerService.this, result);
            }

            mIsCleaning = false;
        }
    }

    public void cleanCache() {
        mIsCleaning = true;
        new TaskClean().execute();
    }
}
