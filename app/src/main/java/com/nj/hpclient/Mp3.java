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
    public static MediaPlayer elephant;
    public static MediaPlayer lion;
    public static MediaPlayer tigger;
    public static MediaPlayer leopard;
    public static MediaPlayer wolf;
    public static MediaPlayer dog;
    public static MediaPlayer cat;
    public static MediaPlayer mouse;
    public static MediaPlayer walkLand;
    public static MediaPlayer walkRiver;
    public static MediaPlayer eatLand;
    public static MediaPlayer eatRiver;

    public static void init(Context context) {
        bgm = MediaPlayer.create(context, R.raw.bgm);
        bgm.setLooping(true);
        win = MediaPlayer.create(context, R.raw.gamewin);
        lose = MediaPlayer.create(context, R.raw.gamelose);
        elephant = MediaPlayer.create(context, R.raw.elephant);
        lion = MediaPlayer.create(context, R.raw.lion);
        tigger = MediaPlayer.create(context, R.raw.tigger);
        leopard = MediaPlayer.create(context, R.raw.leopard);
        wolf = MediaPlayer.create(context, R.raw.wolf);
        dog = MediaPlayer.create(context, R.raw.dog);
        cat = MediaPlayer.create(context, R.raw.cat);
        mouse = MediaPlayer.create(context, R.raw.mouse);
        walkLand = MediaPlayer.create(context, R.raw.walk_land);
        walkRiver = MediaPlayer.create(context, R.raw.walk_river);
        eatLand = MediaPlayer.create(context, R.raw.eat_land);
        eatRiver = MediaPlayer.create(context, R.raw.eat_river);
    }
}
