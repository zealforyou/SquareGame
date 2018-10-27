package com.zz.squarebrick;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.Log;

import java.io.IOException;

/**
 * Created by zhuo.zhang on 2018/10/24.
 */

public class SoundManager {
    Context context;
    private boolean init;
    private SoundPool soundPool;
    private MediaPlayer player;

    private int xiaochu, chaoji, nice, game_in2, button_sound;
    private int sound_Eliminate1, sound_Eliminate2, sound_Eliminate3, sound_Eliminate4;
    private int sound_n_good, sound_n_great, sound_n_absolute, sound_n_amazing, sound_n_unbelievable;

    public SoundManager(Context context) {
        this.context = context;
        soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        player = new MediaPlayer();
        player.setLooping(true);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    public void init() {
        nice = soundPool.load(context, R.raw.nice, 88);
        game_in2 = soundPool.load(context, R.raw.game_in2, 88);
        chaoji = soundPool.load(context, R.raw.chaoji, 99);
        xiaochu = soundPool.load(context, R.raw.sound_create_strip, 100);
        sound_Eliminate1 = soundPool.load(context, R.raw.sound_eliminate1, 100);
        sound_Eliminate2 = soundPool.load(context, R.raw.sound_eliminate2, 100);
        sound_Eliminate3 = soundPool.load(context, R.raw.sound_eliminate3, 100);
        sound_Eliminate4 = soundPool.load(context, R.raw.sound_eliminate4, 100);

        sound_n_good = soundPool.load(context, R.raw.sound_n_good, 100);
        sound_n_great = soundPool.load(context, R.raw.sound_n_great, 100);
        sound_n_absolute = soundPool.load(context, R.raw.sound_n_absolute, 100);
        sound_n_amazing = soundPool.load(context, R.raw.sound_n_amazing, 100);
        sound_n_unbelievable = soundPool.load(context, R.raw.sound_n_unbelievable, 100);

        button_sound = soundPool.load(context, R.raw.button_sound, 100);

        init = true;
    }

    public void playBgm(String assets) {
        if (init) {
            if (player.isPlaying())
                player.stop();
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor fileDescriptor = null;
            try {
                fileDescriptor = assetManager.openFd(assets);
                player.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(),
                        fileDescriptor.getLength());
                player.prepare();
                player.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.i("www", "音乐管理器未初始化");
        }
    }

    /**
     * 播放消除音
     *
     * @param count 消除的个数
     */
    public void playEliminate(int count) {
        if (!init) return;
        int sound = -2;
        switch (count) {
            case 1:
                sound = sound_Eliminate1;
                break;
            case 2:
                sound = sound_Eliminate2;
                break;
            case 3:
                sound = sound_Eliminate3;
                break;
            case 4:
                sound = sound_Eliminate4;
                break;
        }
        if (sound != -2)
            soundPool.play(sound, 1, 1, 100, 0, 1);
    }

    /**
     * 播放奖励音
     *
     * @param count 消除的个数
     */
    public void playN(int count) {
        if (!init) return;
        switch (count) {
            case 2:
                soundPool.play(sound_n_great, 1, 1, 100, 0, 1);
                break;
            case 3:
                soundPool.play(sound_n_amazing, 1, 1, 100, 0, 1);
                break;
            case 4:
                soundPool.play(sound_n_unbelievable, 1, 1, 100, 0, 1);
                break;
        }
    }

    public void gameOver() {
        if (!init) return;
        soundPool.play(chaoji, 1, 1, 100, 0, 1);
        player.stop();
    }

    public void gameInto() {
        if (!init) return;
        soundPool.play(game_in2, 1, 1, 100, 0, 1);
    }

    public void buttonSound() {
        if (!init) return;
        soundPool.play(button_sound, 1, 1, 100, 0, 1);
    }

    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        if (player != null) {
            player.stop();
            player.release();
        }
        init = false;
    }
}
