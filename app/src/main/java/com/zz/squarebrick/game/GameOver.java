package com.zz.squarebrick.game;

/**
 * Created by zhuo.zhang on 2018/10/31.
 */

public class GameOver {
    public static final int RESULT_STATUS_WIN = 0;
    public static final int RESULT_STATUS_LOSE = 1;
    private int result;
    private int score;

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
