package com.java.guohao;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;


public class MyApplication extends Application {
    public void onCreate() {
        super.onCreate();
        int mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        if (Storage.contains(this, GlobVar.NIGHT_MODE_KEY)) {
            mode = Integer.parseInt(Storage.load(this, GlobVar.NIGHT_MODE_KEY));
        }
        AppCompatDelegate.setDefaultNightMode(mode);
    }
}
