package com.zz.squarebrick;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

public class SplashActivity extends AppCompatActivity {
    int waitTime = 4000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        new Thread(new Runnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                long l = System.currentTimeMillis() - startTime;
                SoundManager soundManager = GameApplication.getApp().getSoundManager();
                soundManager.init();
                soundManager.gameInto();
                if (l > waitTime) {
                    intoGame();
                } else {
                    getWindow().getDecorView().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            intoGame();
                        }
                    }, waitTime - l);
                }
            }
        }).start();


    }

    private void intoGame() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
