package com.zz.squarebrick;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.zz.squarebrick.game.Square;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by zhuo.zhang on 2018/10/22.
 */

public class Game2 extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    public interface GameListener {
        void onScore(int score);
    }

    private MediaPlayer mp = new MediaPlayer();
    SoundPool soundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
    private SurfaceHolder holder;
    private boolean init;
    private boolean runing;
    private Paint paint;
    private int columns = 12;
    private int rows = 18;
    private int cellWidth;
    private LinkedList<Square.Cell> cells;
    private Square currentCell;
    private int[] limit = new int[rows];
    private boolean quikDown;
    private int xiaochu, chaoji, nice;
    private int score;
    private GameListener gameListener;

    public Game2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setGameListener(GameListener gameListener) {
        this.gameListener = gameListener;
    }

    public int getScore() {
        return score;
    }

    private void init() {
        cells = new LinkedList<>();
        holder = getHolder();
        holder.addCallback(this);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        nice = soundPool.load(getContext(), R.raw.nice, 88);
        chaoji = soundPool.load(getContext(), R.raw.chaoji, 99);
        xiaochu = soundPool.load(getContext(), R.raw.xiaochu, 100);
    }

    private void playBgm() {
        try {
            mp.setLooping(true);
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            AssetManager assetManager = getContext().getAssets();
            AssetFileDescriptor fileDescriptor = assetManager.openFd("bgm.mp3");
            mp.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(),
                    fileDescriptor.getLength());
//            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                @Override
//                public void onPrepared(MediaPlayer mp) {
//
//                }
//            });
            mp.prepare();
            Game2.this.mp.start();

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        init = true;
        runing = true;
        initCell();
        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        init = false;
        runing = false;
        mp.stop();
        mp.release();
        mp = null;
        soundPool.release();
        soundPool = null;
    }

    private void initCell() {
        cellWidth = getWidth() / columns;
        currentCell = Square.generate(columns);
    }

    @Override
    public void run() {
        playBgm();
        while (runing) {
            draw();
            control();
            try {
                if (quikDown) {
                } else {
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void rotate() {
        if (currentCell.canRotate(90, cells, columns)) {
            currentCell.rotate(90);
            draw();
        }
    }

    public void quikDown() {
        quikDown = true;
    }

    public void moveRight() {
        if (currentCell.canMoveRight(cells, columns)) {
            currentCell.moveRight();
            draw();
        }
    }

    public void moveLeft() {
        if (currentCell.canMoveLeft(cells)) {
            currentCell.moveLeft();
            draw();
        }
    }

    private void fade() {
        int count = 0;
        //检测可消除的单元方块
        int topRow = 0;
        for (int i = limit.length - 1; i >= 0; i--) {
            if (limit[i] == columns) {
                count++;
                limit[i] = 0;
                deleteCell(i);
            }
            if (limit[i] != 0) {
                topRow = i;
            }
        }
        //填补空行
        for (int i = topRow; i < limit.length; i++) {
            if (limit[i] == 0) {
                moveAllCell(i);
            }
        }
        Log.i("www", "count:" + count);
        //优化统计
        if (count > 0) {
            score += count;
            if (count > 1) {
                soundPool.play(nice, 1, 1, 100, 0, 1);
            }
            if (gameListener != null) {
                gameListener.onScore(score);
            }
            soundPool.play(xiaochu, 1, 1, 100, 0, 1);
            for (int i = 0; i < limit.length; i++) {
                limit[i] = 0;
            }
            for (int i = 0; i < cells.size(); i++) {
                Square.Cell cell = cells.get(i);
                limit[cell.getRow()]++;
            }
        }

    }

    private void deleteCell(int row) {
        Iterator<Square.Cell> iterator = cells.iterator();
        while (iterator.hasNext()) {
            Square.Cell next = iterator.next();
            if (next.getRow() == row) {
                iterator.remove();
            }
        }
    }

    private void moveAllCell(int row) {
        for (int i = 0; i < cells.size(); i++) {
            Square.Cell cell = cells.get(i);
            if (cell.getRow() < row)
                cell.setRow(cell.getRow() + 1);
        }
    }

    private synchronized void draw() {
        Canvas canvas = holder.lockCanvas();
        canvas.drawColor(Color.BLACK);
        paint.setColor(Color.WHITE);
        drawCellStroke(canvas);
        drawSquare(canvas);
        holder.unlockCanvasAndPost(canvas);
    }

    private void drawCellStroke(Canvas canvas) {
        for (int i = 0; i < rows + 1; i++) {
            canvas.drawLine(0, i * cellWidth, getWidth(), i * cellWidth, paint);
        }
        for (int j = 0; j < columns + 1; j++) {
            canvas.drawLine(j * cellWidth, 0, j * cellWidth, rows * cellWidth, paint);
        }
    }

    private void drawSquare(Canvas canvas) {
        drawSquare0(canvas, currentCell);
        for (int i = 0; i < cells.size(); i++) {
            drawCell(canvas, cells.get(i));
        }
    }

    //下移控制
    private void control() {
        if (currentCell.canMoveDown(cells, rows)) {
            currentCell.move();
        } else {
            quikDown = false;
            for (int i = 0; i < currentCell.cells.length; i++) {
                cells.add(new Square.Cell(currentCell.cells[i][0], currentCell.cells[i][1], currentCell.color));
                limit[currentCell.cells[i][0]]++;
            }
            fade();
            currentCell = Square.generate(columns);
            draw();
            if (!currentCell.canMoveDown(cells, rows)) {
                runing = false;
                post(new Runnable() {
                    @Override
                    public void run() {
                        mp.stop();
                        soundPool.play(chaoji, 1, 1, 100, 0, 1);
                        Toast.makeText(getContext(), "游戏结束", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private void drawSquare0(Canvas canvas, Square square) {
        for (int i = 0; i < square.cells.length; i++) {
            int[] cell = square.cells[i];
            paint.setColor(square.color);
            canvas.drawRect(cell[1] * cellWidth, cell[0] * cellWidth,
                    cell[1] * cellWidth + cellWidth, cell[0] * cellWidth + cellWidth, paint);
        }
    }

    private void drawCell(Canvas canvas, Square.Cell cell) {
        paint.setColor(cell.color);
        canvas.drawRect(cell.getCol() * cellWidth, cell.getRow() * cellWidth,
                cell.getCol() * cellWidth + cellWidth, cell.getRow() * cellWidth + cellWidth, paint);
    }
}
