package com.zz.squarebrick.game;

/**
 * Created by zhuo.zhang on 2018/10/26.
 */

public class GameMsg {
    private int action;
    private int score;
    private Prop prop;

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public Prop getProp() {
        return prop;
    }

    public void setProp(Prop prop) {
        this.prop = prop;
    }
}
