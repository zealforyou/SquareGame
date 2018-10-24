package com.zz.squarebrick;

import android.app.Application;

/**
 * Created by zhuo.zhang on 2018/10/24.
 */

public class GameApplication extends Application {
    private static GameApplication app;
    private SoundManager soundManager;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        soundManager = new SoundManager(this);
    }

    public static GameApplication getApp() {
        return app;
    }

    public SoundManager getSoundManager() {
        if (soundManager == null) {
            synchronized (this) {
                if (soundManager == null) {
                    soundManager = new SoundManager(this);
                }
            }
        }

        return soundManager;
    }

    public void setSoundManager(SoundManager soundManager) {
        this.soundManager = soundManager;
    }
}
