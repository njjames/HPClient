package com.nj.hpclient;

import android.content.Context;
import android.media.MediaPlayer;

/**
 * Created by Administrator on 2018-04-16.
 */

public class Mp3 {

    public static MediaPlayer bgm;
    public static MediaPlayer win;
    public static MediaPlayer lose;

    public static void init(Context context) {
        bgm = MediaPlayer.create(context, R.raw.bgm);
        bgm.setLooping(true);
        win = MediaPlayer.create(context, R.raw.gamewin);
        lose = MediaPlayer.create(context, R.raw.gamelose);
    }
}
