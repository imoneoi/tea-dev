package com.java.guohao;

import android.content.Context;
import android.content.SharedPreferences;

public class Storage {
    private static final String SP_NAME = "storage";

    public static boolean save(Context context, String key, String value) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        return editor.commit();
    }

    public static boolean contains(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        return sp.contains(key);
    }

    public static String load(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        return sp.getString(key, "");
    }
}
