package com.zz.squarebrick.online;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.gson.Gson;
import com.vise.basebluetooth.BluetoothChatHelper;
import com.vise.basebluetooth.CommandHelper;
import com.vise.basebluetooth.callback.IChatCallback;
import com.vise.basebluetooth.common.BleState;
import com.vise.basebluetooth.mode.BaseMessage;
import com.vise.basebluetooth.utils.HexUtil;
import com.vise.common_utils.log.LogUtils;
import com.zz.squarebrick.GameApplication;
import com.zz.squarebrick.R;
import com.zz.squarebrick.game.Actions;
import com.zz.squarebrick.game.GameMsg;
import com.zz.squarebrick.game.GameOver;

import java.io.UnsupportedEncodingException;

//联机版
public class OnlineGameActivity extends AppCompatActivity {

    private OnlineGame2 gameView;
    private View btn_rotate;
    private View btn_move_left;
    private View btn_move_right;
    private View btn_move_quick;
    private TextView tv_score, tv_score_other, tv_pk_score;
    private Runnable quickLeft, quickRight;
    private BluetoothChatHelper bluetoothChatHelper;
    private Gson gson = new Gson();
    private int pkSocre = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        initView();
        initConnect();
        initRun();
    }

    private void initConnect() {
        if (GameRoomActivity.activity != null) {
            bluetoothChatHelper = GameRoomActivity.activity.getmBluetoothChatHelper();
            bluetoothChatHelper.setmChatCallback(new IChatCallback<byte[]>() {
                @Override
                public void connectStateChange(BleState bleState) {

                }

                @Override
                public void writeData(byte[] data, int type) {

                }

                @Override
                public void readData(byte[] data, int type) {
                    if (data == null) {
                        LogUtils.e("readData is Null or Empty!");
                        return;
                    }
                    LogUtils.i("readData:" + HexUtil.encodeHexStr(data));
                    try {
                        BaseMessage message = CommandHelper.unpackData(data);
                        LogUtils.i("message:" + message.getMsgContent());
                        String msgContent = message.getMsgContent();
                        GameMsg gameMsg = gson.fromJson(msgContent, GameMsg.class);
                        onReceive(gameMsg);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void setDeviceName(String name) {

                }

                @Override
                public void showMessage(String message, int code) {

                }
            });
        }
    }

    private void onReceive(GameMsg msg) {
        final int score = msg.getScore();
        switch (msg.getAction()) {
            case Actions.ACTION_GET_SCORE:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_score_other.setText("对方：" + score);
                    }
                });
                break;
            case Actions.ACTION_GAME_OVER:
                final GameOver gameOver = msg.getGameOver();
                gameView.finishGame();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showGameOverDialog(gameOver, true);
                    }
                });
                break;
        }

    }

    private void sendMessage(GameMsg msg) {
        if (bluetoothChatHelper == null) return;
        try {
            bluetoothChatHelper.write(CommandHelper.packMsg(gson.toJson(msg)));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        gameView = (OnlineGame2) findViewById(R.id.gameView);
        btn_rotate = findViewById(R.id.btn_rotate);
        btn_move_left = findViewById(R.id.btn_move_left);
        btn_move_right = findViewById(R.id.btn_move_right);
        btn_move_quick = findViewById(R.id.btn_move_quick);
        tv_pk_score = (TextView) findViewById(R.id.tv_pk_score);
        tv_score = (TextView) findViewById(R.id.tv_score);
        tv_score_other = (TextView) findViewById(R.id.tv_score_other);
        tv_pk_score.setText("（ " + pkSocre + "分胜利 ）");
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
        gameView.setGameListener(new OnlineGame2.GameListener() {
            @Override
            public void onScore(final int score) {
                if (score >= pkSocre) {
                    GameMsg msg = new GameMsg();
                    msg.setAction(Actions.ACTION_GAME_OVER);
                    final GameOver over = new GameOver();
                    over.setResult(GameOver.RESULT_STATUS_WIN);
                    over.setScore(score);
                    msg.setGameOver(over);
                    gameView.finishGame();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showGameOverDialog(over, false);
                        }
                    });
                    sendMessage(msg);
                } else {
                    GameMsg msg = new GameMsg();
                    msg.setAction(Actions.ACTION_GET_SCORE);
                    msg.setScore(score);
                    sendMessage(msg);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_score.setText("我方：" + score);
                        }
                    });
                }

            }

            @Override
            public void gameOver(int score) {
                GameMsg msg = new GameMsg();
                msg.setAction(Actions.ACTION_GAME_OVER);
                final GameOver over = new GameOver();
                over.setResult(GameOver.RESULT_STATUS_LOSE);
                over.setScore(score);
                msg.setGameOver(over);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showGameOverDialog(over, false);
                    }
                });
                sendMessage(msg);
            }
        });
    }

    private void showGameOverDialog(GameOver gameOver, boolean opponent) {
        GameApplication.getApp().getSoundManager().gameOver();
        Dialog dialog = new Dialog(this);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                finish();
            }
        });
        View inflate = getLayoutInflater().inflate(R.layout.dialog_game_over, null);
        TextView tv_game_win = (TextView) inflate.findViewById(R.id.tv_game_win);
        //对手
        if (opponent) {
            if (gameOver.getResult() == GameOver.RESULT_STATUS_WIN) {
                tv_game_win.setText("你输了");
            } else {
                tv_game_win.setText("你赢了");
            }
        } else {
            if (gameOver.getResult() == GameOver.RESULT_STATUS_LOSE) {
                tv_game_win.setText("你输了");
            } else {
                tv_game_win.setText("你赢了");
            }
        }

        dialog.setContentView(inflate);
        dialog.show();
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

    @Override
    public void onBackPressed() {
        GameMsg msg = new GameMsg();
        msg.setAction(Actions.ACTION_GAME_OVER);
        GameOver over = new GameOver();
        over.setResult(GameOver.RESULT_STATUS_LOSE);
        over.setScore(0);
        msg.setGameOver(over);
        sendMessage(msg);
        btn_rotate.postDelayed(new Runnable() {
            @Override
            public void run() {
                OnlineGameActivity.super.onBackPressed();
            }
        }, 300);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GameApplication.getApp().getSoundManager().stopAll();
        bluetoothChatHelper = null;
    }
}
