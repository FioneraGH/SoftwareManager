package com.fionera.cleaner.utils;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

import com.fionera.cleaner.bean.SDCardInfo;
import com.fionera.cleaner.bean.StorageSizeInfo;

import java.io.File;

public class StorageUtil {

    public static String convertStorage(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;

        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else {
            return String.format("%d B", size);
        }
    }

    public static StorageSizeInfo convertStorageSize(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;
        StorageSizeInfo sto = new StorageSizeInfo();
        if (size >= gb) {

            sto.suffix = "GB";
            sto.value = (float) size / gb;
            return sto;
        } else if (size >= mb) {

            sto.suffix = "MB";
            sto.value = (float) size / mb;

            return sto;
        } else if (size >= kb) {


            sto.suffix = "KB";
            sto.value = (float) size / kb;

            return sto;
        } else {
            sto.suffix = "B";
            sto.value = (float) size;

            return sto;
        }
    }

    public static SDCardInfo getSDCardInfo() {
        if (Environment.isExternalStorageRemovable()) {
            String sDcString = Environment.getExternalStorageState();
            if (sDcString.equals(Environment.MEDIA_MOUNTED)) {
                File pathFile = Environment.getExternalStorageDirectory();

                try {
                    StatFs statfs = new StatFs(pathFile.getPath());
                    SDCardInfo info = new SDCardInfo();
                    info.total = statfs.getTotalBytes();
                    info.free = statfs.getAvailableBytes();
                    return info;
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static SDCardInfo getSystemSpaceInfo() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        SDCardInfo info = new SDCardInfo();
        info.total = stat.getTotalBytes();
        info.free = stat.getAvailableBytes();
        return info;
    }
}
