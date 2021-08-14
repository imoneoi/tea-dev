package com.java.guohao;

import android.app.Application;

import com.xuexiang.xui.XUI;

public class MyApplication extends Application {
    public void onCreate() {
        super.onCreate();
        XUI.init(this);
        XUI.debug(true);
    }
}
