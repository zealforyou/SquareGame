package com.zz.squarebrick;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.zz.squarebrick.game.Square;
import com.zz.squarebrick.particle.factory.ParticleFactory;
import com.zz.squarebrick.particle.factory.VerticalAscentFactory;
import com.zz.squarebrick.particle.particle.Particle;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

/**
 * Created by zhuo.zhang on 2018/10/22.
 */

public class Game2 extends SurfaceView implements SurfaceHolder.Callback, Runnable {


    private Thread thread;
    private Matrix matrix;

    public interface GameListener {
        void onScore(int score);
    }

    private SurfaceHolder holder;
    private boolean init;
    private boolean runing;
    private Paint paint;
    private Paint explodePaint;
    private int columns = 12;
    private int rows = 18;
    private int cellWidth;
    private LinkedList<Square.Cell> cells;
    private Square currentCell;
    private int[] limit = new int[rows];
    private boolean quikDown;
    private int score;
    private GameListener gameListener;
    private Bitmap gameBg;
    private SoundManager soundManager;

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
        soundManager = GameApplication.getApp().getSoundManager();
        cells = new LinkedList<>();
        holder = getHolder();
        holder.addCallback(this);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        explodePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int bg[] = {R.mipmap.game_bg1, R.mipmap.game_bg2};
        gameBg = BitmapFactory.decodeResource(getResources(), bg[(int) (Math.random() * bg.length)]);
        matrix = new Matrix();
        int height = gameBg.getHeight();
        int heightPixels = getResources().getDisplayMetrics().heightPixels;
        int widthPixels = getResources().getDisplayMetrics().widthPixels;
        float v = 1f * heightPixels / height;
        matrix.preScale(v, v, 0, 0);
        matrix.postTranslate(widthPixels / 2 - gameBg.getWidth() / 2, 0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        cellWidth = getMeasuredWidth() / columns;
        int height = rows * cellWidth;
        setMeasuredDimension(getMeasuredWidth(), height + 50);
    }

    private void playBgm() {
        soundManager.playBgm("bgm.mp3");
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        init = true;
        runing = true;
        initCell();
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        init = false;
        runing = false;
        thread.interrupt();
        soundManager.gameOver();
        soundManager.release();
        GameApplication.getApp().setSoundManager(null);
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
            try {
                if (quikDown) {
                } else {
                    Thread.sleep(600);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            control();
        }
    }

    public void rotate() {
        if (currentCell != null) {
            int[][] ints = currentCell.canRotate(90, cells, columns, rows);
            if (ints != null) {
                currentCell.rotate(90, ints);
                draw();
            }
        }

    }

    public void quikDown() {
        quikDown = true;
        if (thread != null) {
            thread.interrupt();
        }
    }

    public void moveRight() {
        if (currentCell != null && currentCell.canMoveRight(cells, columns)) {
            currentCell.moveRight();
            draw();
        }
    }

    public void moveLeft() {
        if (currentCell != null && currentCell.canMoveLeft(cells)) {
            currentCell.moveLeft();
            draw();
        }
    }

    private void fade() {
        int count = 0;

        //检测可消除的单元方块
        int fadeRow = 0;
        while ((fadeRow = hasFadeRow()) != -1) {
            //删除单元格
            count++;
            score++;
            if (gameListener != null) {
                gameListener.onScore(score * 100);
            }
            explode(fadeRow);
            soundManager.playEliminate(count);
            explode2(fadeRow);
            deleteRowCell(fadeRow);
            moveAllCell(fadeRow);
            draw();
            refreshLimit();
        }
        soundManager.playN(count);
        Log.i("www", "count:" + count);
    }

    /**
     * 是否有可消除的行
     *
     * @return 可消除的行号
     */
    private int hasFadeRow() {
        for (int i = limit.length - 1; i >= 0; i--) {
            if (limit[i] == columns) {
                limit[i] = 0;
                return i;
            }
        }
        return -1;
    }

    //刷新行限制检测
    private void refreshLimit() {
        for (int i = 0; i < limit.length; i++) {
            limit[i] = 0;
        }
        for (int i = 0; i < cells.size(); i++) {
            Square.Cell cell = cells.get(i);
            limit[cell.getRow()]++;
        }
    }

    //删除某一行的单元格
    private void deleteRowCell(int row) {
        Iterator<Square.Cell> iterator = cells.iterator();
        while (iterator.hasNext()) {
            Square.Cell next = iterator.next();
            if (next.getRow() == row) {
                iterator.remove();
            }
        }
    }

    //移动单元格 填补空行
    private void moveAllCell(int row) {
        for (int i = 0; i < cells.size(); i++) {
            Square.Cell cell = cells.get(i);
            if (cell.getRow() < row)
                cell.setRow(cell.getRow() + 1);
        }
    }

    private synchronized void draw() {
        draw(0, 0);
    }

    /**
     * @param transX
     * @param transY
     */
    private synchronized void draw(float transX, float transY) {
        Canvas canvas = holder.lockCanvas();
        canvas.drawColor(Color.BLACK);
        canvas.drawBitmap(gameBg, matrix, paint);
        paint.setColor(Color.WHITE);
        drawCellStroke(canvas);
        if (currentCell != null)
            drawSquare0(canvas, currentCell);
        for (int i = 0; i < cells.size(); i++) {
            Square.Cell cell = cells.get(i);
            drawCell(canvas, cell.getRow(), cell.getCol(), cell.color, transX, transY);
        }
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
            currentCell = null;
            fade();
            //生成新的可运动方块
            currentCell = Square.generate(columns);
            draw();
            if (!currentCell.canMoveDown(cells, rows)) {
                runing = false;
                post(new Runnable() {
                    @Override
                    public void run() {
                        soundManager.gameOver();
                        Toast.makeText(getContext(), "游戏结束", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private void drawSquare0(Canvas canvas, Square square) {
        for (int i = 0; i < square.cells.length; i++) {
            int[] cell = square.cells[i];
            drawCell(canvas, cell[0], cell[1], square.color);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void drawCell(Canvas canvas, int row, int col, int color) {
        drawCell(canvas, row, col, color, 0, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void drawCell(Canvas canvas, int row, int col, int color, float transX, float transY) {
        int radius = 10;
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(col * cellWidth + transX, row * cellWidth + transY,
                col * cellWidth + cellWidth + transX, row * cellWidth + cellWidth + transY, radius, radius, paint);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        canvas.drawRoundRect(col * cellWidth + transX, row * cellWidth + transY,
                col * cellWidth + cellWidth + transX, row * cellWidth + cellWidth + transY, radius, radius, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(0);
    }

    /**
     * 爆破
     */
    public void explode(int row) {
        //防止重复点击
        long startTime = System.currentTimeMillis();
        int duration = 100;
        Random random = new Random();
        while (System.currentTimeMillis() - startTime < duration) {
            float transX = (random.nextFloat() - 0.5f) * getWidth() * 0.01f;
            float transY = (random.nextFloat() - 0.5f) * getHeight() * 0.01f;
            Canvas canvas = holder.lockCanvas();
            canvas.drawColor(Color.BLACK);
            canvas.drawBitmap(gameBg, matrix, paint);
            paint.setColor(Color.WHITE);
            drawCellStroke(canvas);
            if (currentCell != null)
                drawSquare0(canvas, currentCell);
            for (int i = 0; i < cells.size(); i++) {
                Square.Cell cell = cells.get(i);
                if (cell.getRow() == row)
                    drawCell(canvas, cell.getRow(), cell.getCol(), cell.color, transX, transY);
                else
                    drawCell(canvas, cell.getRow(), cell.getCol(), cell.color);
            }
            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void explode2(int row) {
//        view.animate().setDuration(150).scaleX(0f).scaleY(0f).alpha(0f).start();
        int random = (int) (Math.random() * 6);
        ParticleFactory mParticleFactory = new VerticalAscentFactory();
//        switch (random) {
//            case 0:
//                mParticleFactory = new BooleanFactory();
//                break;
//            case 1:
//                mParticleFactory = new ExplodeParticleFactory();
//                break;
//            case 2:
//                mParticleFactory = new FlyawayFactory();
//                break;
//            case 3:
//                mParticleFactory = new InnerFallingParticleFactory();
//                break;
//            case 4:
//                mParticleFactory = new VerticalAscentFactory();
//                break;
//            case 5:
//                mParticleFactory = new FallingParticleFactory();
//                break;
//        }

        Rect rect = new Rect(0, row * cellWidth, getWidth(), row * cellWidth + cellWidth);
        Particle[][] particles = mParticleFactory.generateParticles(cells, rect, row, cellWidth);
        //所有粒子运动
        long startTime = System.currentTimeMillis();
        int duration = 300;
        long rate;

        while ((rate = System.currentTimeMillis() - startTime) <= duration) {
            Canvas canvas = holder.lockCanvas();
            canvas.drawColor(Color.BLACK);
            canvas.drawBitmap(gameBg, matrix, paint);
            paint.setColor(Color.WHITE);
            drawCellStroke(canvas);
            if (currentCell != null)
                drawSquare0(canvas, currentCell);
            for (int i = 0; i < cells.size(); i++) {
                Square.Cell cell = cells.get(i);
                if (cell.getRow() != row)
                    drawCell(canvas, cell.getRow(), cell.getCol(), cell.color);
            }
            for (Particle[] particle : particles) {
                for (Particle p : particle) {
                    p.advance(canvas, explodePaint, 1f * rate / duration);
                }
            }
            holder.unlockCanvasAndPost(canvas);
        }
    }
}
