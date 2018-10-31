package com.zz.squarebrick.outline;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import com.zz.squarebrick.GameApplication;
import com.zz.squarebrick.R;

public class GamePrepareActivity extends AppCompatActivity {


    private View btn_start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game_prepare);
        initView();
    }


    private void initView() {
        btn_start = findViewById(R.id.btn_start);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GameApplication.getApp().getSoundManager().buttonSound();
                btn_start.setClickable(false);
                btn_start.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        btn_start.setClickable(true);
                        startActivity(new Intent(GamePrepareActivity.this, PlayActivity.class));
                    }
                }, 500);

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
