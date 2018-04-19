package com.nj.hpclient;

import java.util.ArrayList;

/**
 * Created by nj on 2018/3/30.
 */

public class Game {
    //步数
    private int step;
    //步时
    private long walktime;

    //玩家1
    private User user1;

    //玩家2
    private User user2;

    //玩家1的牌数
    public int count1;

    //玩家2的牌数
    public int count2;

    //地图(也是当前的地图)
    private int[][] map;

    //上一步棋牌的地图
    private int[][] lastmap;

    //走棋的步骤
    private ArrayList<Walk> walks;

    private Walk walk;

    public Game() {
        super();
    }

    public Game(User user1, User user2) {
        this.user1 = user1;
        this.user2 = user2;
        startGame();
    }

    private void startGame() {
        this.count1 = 8;
        this.count2 = 8;
        this.step = 0;
        this.lastmap = null;
//        this.map = GameUtil.cloneMap(GameUtil.DEFAULT_MAP);
        this.walks = new ArrayList<>();
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public long getWalktime() {
        return walktime;
    }

    public void setWalktime(long walktime) {
        this.walktime = walktime;
    }

    public int[][] getMap() {
        return map;
    }

    public void setMap(int[][] map) {
        this.map = map;
    }

    @Override
    public String toString() {
        Walk w = null;
        //如果走过棋了，就取出最后一次的一步，否则初始化一个
        if(walks.size() > 0) {
            w = walks.get(walks.size() - 1);
        }else {
            w = new Walk(-1, -1, -1, -1);
        }
        return step + ";" + w + ";" + getMapString();
    }

    /*
     * 得到地图的字符串
     */
    private String getMapString() {
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 9; j++) {
                s.append(map[i][j] + ",");
            }
        }
        //删除最后一个逗号
        s.delete(s.length() - 1, s.length());
        return s.toString();
    }

    /**
     * 根据服务器返回的字符串，得到两个玩家的信息
     * @param content
     */
    public void setUser(String content) throws Exception {
        String[] users = content.split(";");
        user1 = User.fromString(users[0]);
        user2 = User.fromString(users[1]);
    }

    /**
     * 根据自身获取对方玩家
     * @param self
     * @return
     */
    public User getOtherUser(User self) {
        if (self.equals(user1)) {
            return user2;
        }else {
            return user1;
        }
    }

    public User getUser1() {
        return user1;
    }

    public User getUser2() {
        return user2;
    }

    public void setData(String content) throws Exception {
        String[] tag = content.split(";");
        this.step = Integer.parseInt(tag[0]);
        this.walk = Walk.fromString(tag[1]);
        this.map = GameUtil.mapFromString(tag[2]);
    }
}
