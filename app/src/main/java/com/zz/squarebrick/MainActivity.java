package com.zz.squarebrick;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    private Game2 gameView;
    private View btn_rotate;
    private View btn_move_left;
    private View btn_move_right;
    private View btn_move_quick;
    private TextView tv_score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
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
        btn_move_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameView.moveLeft();
            }
        });
        btn_move_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameView.moveRight();
            }
        });
        btn_move_quick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameView.quikDown();
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
}
