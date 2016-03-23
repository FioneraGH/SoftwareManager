package com.fionera.cleaner.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import com.fionera.cleaner.bean.AutoStartInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BootStartUtils {

    private static final String BOOT_START_PERMISSION = "android.permission.RECEIVE_BOOT_COMPLETED";

    public static List<AutoStartInfo> fetchInstalledApps(Context mContext) {
        PackageManager pm = mContext.getPackageManager();
        List<ApplicationInfo> appInfoList = pm.getInstalledApplications(0);
        Iterator<ApplicationInfo> appInfoIterator = appInfoList.iterator();
        List<AutoStartInfo> autoStartInfoList = new ArrayList<>(appInfoList.size());

        while (appInfoIterator.hasNext()) {
            ApplicationInfo app = appInfoIterator.next();
            AutoStartInfo autoStartInfo = new AutoStartInfo();
            autoStartInfo.setLabel(pm.getApplicationLabel(app).toString());
            autoStartInfo.setPackageName(app.packageName);
            autoStartInfo.setIcon(pm.getApplicationIcon(app));
            /**
             * 开机自启动，权限描述不完整
             */
            autoStartInfo.setEnable(pm.checkPermission(BOOT_START_PERMISSION,
                                                       app.packageName) == PackageManager
                    .PERMISSION_GRANTED);
            autoStartInfo.setSystem((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
            autoStartInfoList.add(autoStartInfo);

        }
        return autoStartInfoList;
    }

    @SuppressWarnings("WrongConstant")
    public static List<AutoStartInfo> fetchAutoApps(Context mContext) {
        PackageManager pm = mContext.getPackageManager();
        List<ResolveInfo> resolveInfoList = pm
                .queryBroadcastReceivers(new Intent(Intent.ACTION_BOOT_COMPLETED),
                                         PackageManager.GET_DISABLED_COMPONENTS);
        List<AutoStartInfo> autoStartInfoList = new ArrayList<>();
        String appName;
        String packageReceiver;
        Drawable icon;
        boolean isSystem;
        boolean isEnable;
        if (resolveInfoList.size() > 0) {

            appName = resolveInfoList.get(0).loadLabel(pm).toString();
            /**
             * pm disable/enable 语法：组件=包名/类名
             */
            packageReceiver = resolveInfoList
                    .get(0).activityInfo.packageName + "/" + resolveInfoList
                    .get(0).activityInfo.name;
            icon = resolveInfoList.get(0).loadIcon(pm);
            isEnable = pm.getComponentEnabledSetting(
                    new ComponentName(resolveInfoList.get(0).activityInfo.packageName,
                                      resolveInfoList
                                              .get(0).activityInfo.name)) != PackageManager
                    .COMPONENT_ENABLED_STATE_DISABLED;
            isSystem = (resolveInfoList
                    .get(0).activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            for (int i = 1; i < resolveInfoList.size(); i++) {
                AutoStartInfo mAutoStartInfo = new AutoStartInfo();
                if (appName.equals(resolveInfoList.get(i).loadLabel(pm).toString())) {
                    packageReceiver = packageReceiver + ";" + resolveInfoList
                            .get(i).activityInfo.packageName + "/" + resolveInfoList
                            .get(i).activityInfo.name;
                } else {
                    mAutoStartInfo.setLabel(appName);
                    mAutoStartInfo.setPackageReceiver(packageReceiver);
                    mAutoStartInfo.setSystem(isSystem);
                    mAutoStartInfo.setIcon(icon);
                    mAutoStartInfo.setEnable(isEnable);

                    autoStartInfoList.add(mAutoStartInfo);

                    appName = resolveInfoList.get(i).loadLabel(pm).toString();
                    /**
                     * pm disable/enable 语法：组件=包名/类名
                     */
                    packageReceiver = resolveInfoList
                            .get(i).activityInfo.packageName + "/" + resolveInfoList
                            .get(i).activityInfo.name;
                    icon = resolveInfoList.get(i).loadIcon(pm);
                    isEnable = pm.getComponentEnabledSetting(
                            new ComponentName(resolveInfoList.get(i).activityInfo.packageName,
                                              resolveInfoList
                                                      .get(i).activityInfo.name)) !=
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
                    isSystem = (resolveInfoList
                            .get(i).activityInfo.applicationInfo.flags & ApplicationInfo
                            .FLAG_SYSTEM) != 0;
                }
            }
            AutoStartInfo mAutoStartInfo = new AutoStartInfo();
            mAutoStartInfo.setLabel(appName);
            mAutoStartInfo.setSystem(isSystem);
            mAutoStartInfo.setEnable(isEnable);
            mAutoStartInfo.setIcon(icon);
            mAutoStartInfo.setPackageReceiver(packageReceiver);
            autoStartInfoList.add(mAutoStartInfo);
        }

        return autoStartInfoList;
    }
}