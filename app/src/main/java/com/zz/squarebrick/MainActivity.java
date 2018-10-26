package com.zz.squarebrick;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    private Game2 gameView;
    private View btn_rotate;
    private View btn_move_left;
    private View btn_move_right;
    private View btn_move_quick;
    private TextView tv_score;
    private Runnable quickLeft, quickRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        initView();
        initRun();
    }

    private void initView() {
        gameView = (Game2) findViewById(R.id.gameView);
        btn_rotate = findViewById(R.id.btn_rotate);
        btn_move_left = findViewById(R.id.btn_move_left);
        btn_move_right = findViewById(R.id.btn_move_right);
        btn_move_quick = findViewById(R.id.btn_move_quick);
        tv_score = (TextView) findViewById(R.id.tv_score);
        btn_rotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameView.rotate();
            }
        });
        //左移
        btn_move_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameView.moveLeft();
            }
        });
        btn_move_left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    btn_move_left.removeCallbacks(quickLeft);
                }
                return false;
            }
        });
        btn_move_left.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                quikLeft();
                return true;
            }
        });
        //右移
        btn_move_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameView.moveRight();
            }
        });
        btn_move_right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    btn_move_right.removeCallbacks(quickRight);
                }
                return false;
            }
        });
        btn_move_right.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                quickRight();
                return true;
            }
        });
        //快速下移
        btn_move_quick.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    gameView.quikDown();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    gameView.quikStop();
                }
                return true;
            }
        });
        gameView.setGameListener(new Game2.GameListener() {
            @Override
            public void onScore(final int score) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_score.setText("得分：" + score);
                    }
                });
            }
        });
    }

    private void initRun() {
        quickLeft = new Runnable() {
            @Override
            public void run() {
                gameView.moveLeft();
                quikLeft();
            }
        };
        quickRight = new Runnable() {
            @Override
            public void run() {
                gameView.moveRight();
                quickRight();
            }
        };
    }

    private void quikLeft() {
        btn_move_left.postDelayed(quickLeft, 50);
    }

    private void quickRight() {
        btn_move_right.postDelayed(quickRight, 50);
    }
}
