package com.zz.squarebrick.online;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.vise.basebluetooth.BluetoothChatHelper;
import com.vise.basebluetooth.CommandHelper;
import com.vise.basebluetooth.callback.IChatCallback;
import com.vise.basebluetooth.common.BleState;
import com.vise.basebluetooth.mode.BaseMessage;
import com.vise.basebluetooth.utils.HexUtil;
import com.vise.common_utils.log.LogUtils;
import com.zz.squarebrick.R;

import java.io.UnsupportedEncodingException;

//联机版
public class OnlineGameActivity extends AppCompatActivity {

    private OnlineGame2 gameView;
    private View btn_rotate;
    private View btn_move_left;
    private View btn_move_right;
    private View btn_move_quick;
    private TextView tv_score;
    private Runnable quickLeft, quickRight;
    private BluetoothChatHelper bluetoothChatHelper;

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
                        LogUtils.i("message-----" + message.getMsgContent());
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

    private void sendMessage(String msg) {
        if (bluetoothChatHelper == null) return;
        try {
            bluetoothChatHelper.write(CommandHelper.packMsg(msg));
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
        gameView.setGameListener(new OnlineGame2.GameListener() {
            @Override
            public void onScore(final int score) {
                sendMessage("得分：" + score);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_score.setText("得分：" + score);
                    }
                });
            }

            @Override
            public void gameOver(int score) {

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothChatHelper = null;
    }
}
