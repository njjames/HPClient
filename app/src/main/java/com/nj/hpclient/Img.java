package com.nj.hpclient;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Img {
	public static Bitmap BG;
	public static Bitmap BOARD;
	public static Bitmap OOS;
	public static Bitmap PK;

	public static Bitmap CHESSBG;
	public static Bitmap BELEPHANT;
	public static Bitmap BLION;
	public static Bitmap BTIGER;
	public static Bitmap BLEOPARD;
	public static Bitmap BWOLF;
	public static Bitmap BDOG;
	public static Bitmap BCAT;
	public static Bitmap BMOUSE;

	public static Bitmap RELEPHANT;
	public static Bitmap RLION;
	public static Bitmap RTIGER;
	public static Bitmap RLEOPARD;
	public static Bitmap RWOLF;
	public static Bitmap RDOG;
	public static Bitmap RCAT;
	public static Bitmap RMOUSE;

	public static Bitmap SELECT_BELEPHANT;
	public static Bitmap SELECT_BLION;
	public static Bitmap SELECT_BTIGER;
	public static Bitmap SELECT_BLEOPARD;
	public static Bitmap SELECT_BWOLF;
	public static Bitmap SELECT_BDOG;
	public static Bitmap SELECT_BCAT;
	public static Bitmap SELECT_BMOUSE;

	public static Bitmap SELECT_RELEPHANT;
	public static Bitmap SELECT_RLION;
	public static Bitmap SELECT_RTIGER;
	public static Bitmap SELECT_RLEOPARD;
	public static Bitmap SELECT_RWOLF;
	public static Bitmap SELECT_RDOG;
	public static Bitmap SELECT_RCAT;
	public static Bitmap SELECT_RMOUSE;

	public static Bitmap HEAD1;
	public static Bitmap HEAD2;
	public static Bitmap HEAD3;
	public static Bitmap HEAD4;
	public static Bitmap HEAD5;
	public static Bitmap HEAD6;
	public static Bitmap HEAD7;
	public static Bitmap HEAD8;
	public static Bitmap HEAD9;
	public static Bitmap HEAD10;
	public static Bitmap HEAD11;
	public static Bitmap HEAD12;
	public static Bitmap HEAD13;
	public static Bitmap HEAD14;

	public static void init(Context context) {
		BG = BitmapFactory.decodeResource(context.getResources(), R.drawable.game_bg);
		CHESSBG = BitmapFactory.decodeResource(context.getResources(), R.drawable.chess_bg);
		PK = BitmapFactory.decodeResource(context.getResources(), R.drawable.pk);
//		BOARD = BitmapFactory.decodeResource(context.getResources(),
//				R.drawable.board);
//		OOS = BitmapFactory.decodeResource(context.getResources(),
//				R.drawable.oos);

		BELEPHANT = BitmapFactory.decodeResource(context.getResources(), R.drawable.elephant_b);
		BLION = BitmapFactory.decodeResource(context.getResources(), R.drawable.lion_b);
		BTIGER = BitmapFactory.decodeResource(context.getResources(), R.drawable.tiger_b);
		BLEOPARD = BitmapFactory.decodeResource(context.getResources(), R.drawable.leopard_b);
		BWOLF = BitmapFactory.decodeResource(context.getResources(), R.drawable.wolf_b);
		BDOG = BitmapFactory.decodeResource(context.getResources(), R.drawable.dog_b);
		BCAT = BitmapFactory.decodeResource(context.getResources(), R.drawable.cat_b);
		BMOUSE = BitmapFactory.decodeResource(context.getResources(), R.drawable.mouse_b);
		RELEPHANT = BitmapFactory.decodeResource(context.getResources(), R.drawable.elephant_r);
		RLION = BitmapFactory.decodeResource(context.getResources(), R.drawable.lion_r);
		RTIGER = BitmapFactory.decodeResource(context.getResources(), R.drawable.tiger_r);
		RLEOPARD = BitmapFactory.decodeResource(context.getResources(), R.drawable.leopard_r);
		RWOLF = BitmapFactory.decodeResource(context.getResources(), R.drawable.wolf_r);
		RDOG = BitmapFactory.decodeResource(context.getResources(), R.drawable.dog_r);
		RCAT = BitmapFactory.decodeResource(context.getResources(), R.drawable.cat_r);
		RMOUSE = BitmapFactory.decodeResource(context.getResources(), R.drawable.mouse_r);

		SELECT_BELEPHANT = BitmapFactory.decodeResource(context.getResources(), R.drawable.elephant_b_select);
		SELECT_BLION = BitmapFactory.decodeResource(context.getResources(), R.drawable.lion_b_select);
		SELECT_BTIGER = BitmapFactory.decodeResource(context.getResources(), R.drawable.tiger_b_select);
		SELECT_BLEOPARD = BitmapFactory.decodeResource(context.getResources(), R.drawable.leopard_b_select);
		SELECT_BWOLF = BitmapFactory.decodeResource(context.getResources(), R.drawable.wolf_b_select);
		SELECT_BDOG = BitmapFactory.decodeResource(context.getResources(), R.drawable.dog_b_select);
		SELECT_BCAT = BitmapFactory.decodeResource(context.getResources(), R.drawable.cat_b_select);
		SELECT_BMOUSE = BitmapFactory.decodeResource(context.getResources(), R.drawable.mouse_b_select);
		SELECT_RELEPHANT = BitmapFactory.decodeResource(context.getResources(), R.drawable.elephant_r_select);
		SELECT_RLION = BitmapFactory.decodeResource(context.getResources(), R.drawable.lion_r_select);
		SELECT_RTIGER = BitmapFactory.decodeResource(context.getResources(), R.drawable.tiger_r_select);
		SELECT_RLEOPARD = BitmapFactory.decodeResource(context.getResources(), R.drawable.leopard_r_select);
		SELECT_RWOLF = BitmapFactory.decodeResource(context.getResources(), R.drawable.wolf_r_select);
		SELECT_RDOG = BitmapFactory.decodeResource(context.getResources(), R.drawable.dog_r_select);
		SELECT_RCAT = BitmapFactory.decodeResource(context.getResources(), R.drawable.cat_r_select);
		SELECT_RMOUSE = BitmapFactory.decodeResource(context.getResources(), R.drawable.mouse_r_select);

		HEAD1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.head1);
		HEAD2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.head2);
		HEAD3 = BitmapFactory.decodeResource(context.getResources(), R.drawable.head3);
		HEAD4 = BitmapFactory.decodeResource(context.getResources(), R.drawable.head4);
		HEAD5 = BitmapFactory.decodeResource(context.getResources(), R.drawable.head5);
		HEAD6 = BitmapFactory.decodeResource(context.getResources(), R.drawable.head6);
		HEAD7 = BitmapFactory.decodeResource(context.getResources(), R.drawable.head7);
		HEAD8 = BitmapFactory.decodeResource(context.getResources(), R.drawable.head8);
		HEAD9 = BitmapFactory.decodeResource(context.getResources(), R.drawable.head9);
		HEAD10 = BitmapFactory.decodeResource(context.getResources(), R.drawable.head10);
		HEAD11 = BitmapFactory.decodeResource(context.getResources(), R.drawable.head11);
		HEAD12 = BitmapFactory.decodeResource(context.getResources(), R.drawable.head12);
		HEAD13 = BitmapFactory.decodeResource(context.getResources(), R.drawable.head13);
		HEAD14 = BitmapFactory.decodeResource(context.getResources(), R.drawable.head14);
	}

	public static Bitmap getHead(int h) {
		switch (h) {
			case 1:
				return HEAD1;
			case 2:
				return HEAD2;
			case 3:
				return HEAD3;
			case 4:
				return HEAD4;
			case 5:
				return HEAD5;
			case 6:
				return HEAD6;
			case 7:
				return HEAD7;
			case 8:
				return HEAD8;
			case 9:
				return HEAD9;
			case 10:
				return HEAD10;
			case 11:
				return HEAD11;
			case 12:
				return HEAD12;
			case 13:
				return HEAD13;
			case 14:
				return HEAD14;
			default:
				return null;
		}
	}
}
