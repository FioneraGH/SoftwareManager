package com.fionera.cleaner.bean;

import android.content.pm.ActivityInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ServiceInfo;
import android.graphics.drawable.Drawable;

import java.util.List;

/**
 * Holder of packageInfo
 */
public class AppInfo {
    private Drawable appIcon;
    private String appName;
    private String packageName;
    private String version;
    private long pkgSize;
    private int uid;
    private boolean userApp;

    private List<String> serviceInfos;
    private List<String> permissionInfos;

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public long getPkgSize() {
        return pkgSize;
    }

    public void setPkgSize(long pkgSize) {
        this.pkgSize = pkgSize;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public boolean isUserApp() {
        return userApp;
    }

    public void setUserApp(boolean userApp) {
        this.userApp = userApp;
    }

    public List<String> getServiceInfos() {
        return serviceInfos;
    }

    public void setServiceInfos(List<String> serviceInfos) {
        this.serviceInfos = serviceInfos;
    }

    public List<String> getPermissionInfos() {
        return permissionInfos;
    }

    public void setPermissionInfos(List<String> permissionInfos) {
        this.permissionInfos = permissionInfos;
    }
}
