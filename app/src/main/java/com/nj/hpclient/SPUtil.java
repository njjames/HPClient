package com.nj.hpclient;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by nj on 2018/3/28.
 */

public class SPUtil {
    private static SharedPreferences sp;

    public static void putString(Context context, String key, String value) {
        if(sp == null) {
            sp = context.getSharedPreferences("userconfig", Context.MODE_PRIVATE);
        }
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getString(Context context, String key, String defValue) {
        if(sp == null) {
            sp = context.getSharedPreferences("userconfig", Context.MODE_PRIVATE);
        }
        return  sp.getString(key, defValue);
    }

    public static void putInt(Context context, String key, int value) {
        if(sp == null) {
            sp = context.getSharedPreferences("userconfig", Context.MODE_PRIVATE);
        }
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static int getInt(Context context, String key, int defValue) {
        if(sp == null) {
            sp = context.getSharedPreferences("userconfig", Context.MODE_PRIVATE);
        }
        return  sp.getInt(key, defValue);
    }

}
