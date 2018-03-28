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
//            sp = context.getSharedPreferences();
        }
    }
}
