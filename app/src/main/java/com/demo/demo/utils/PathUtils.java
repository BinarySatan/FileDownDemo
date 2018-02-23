package com.demo.demo.utils;

import android.os.Environment;

import com.demo.demo.MyApplication;

import java.io.File;

/**
 * Author:BinarySatan
 * Time: 2017/9/6
 */

public class PathUtils {

    /**
     * 创建根缓存目录
     *
     * @return
     */
    public static String getRootPath() {
        return realRootPath(false);
    }

    public static String getRootAbsPath() {
        return realRootPath(true);
    }

    private static String realRootPath(boolean abs) {
        String cacheRootPath = "";
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            // /sdcard/Android/data/<application package>/cache
            File file = MyApplication.mContext.getExternalCacheDir();
            if (file != null)
                cacheRootPath = abs ? file.getAbsolutePath() : file.getPath();
        } else {
            // /data/data/<application package>/cache
            cacheRootPath = abs ? MyApplication.mContext.getCacheDir().getAbsolutePath() : MyApplication.mContext.getCacheDir().getPath();
        }
        return cacheRootPath;
    }


    public static String getChapterPath(String bookId) {
        String cacheChapterPath = "";

        File newFile = new File(getRootPath() + File.separator + "book" + File.separator +
                bookId + File.separator + "chapter");
        if (!newFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            newFile.mkdirs();
        }
        cacheChapterPath = newFile.getPath();
        return cacheChapterPath;
    }


    public static String getMagazinePath() {
        String magazinePath;
        File newFile = new File(getRootPath(), "magazine");
        if (!newFile.exists()) {
            newFile.mkdirs();
        }
        magazinePath = newFile.getAbsolutePath();
        return magazinePath;
    }

    public static String getMagazinePdfPath(String itemId) {
        File newFile = new File(getMagazinePath(), "pdf");
        if (!newFile.exists()) {
            newFile.mkdirs();
        }
        return newFile.getAbsolutePath() + File.separator + itemId + ".pdf";
    }


}
