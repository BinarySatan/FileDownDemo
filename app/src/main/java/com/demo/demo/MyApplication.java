package com.demo.demo;

import android.app.Application;
import android.content.Context;

/**
 * Author:BinarySatan
 * Time: 2018/2/23
 */

public class MyApplication extends Application {

    public static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;
    }
}
