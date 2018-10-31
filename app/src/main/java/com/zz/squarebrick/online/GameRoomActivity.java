package com.zz.squarebrick.online;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.vise.basebluetooth.BluetoothChatHelper;
import com.vise.basebluetooth.CommandHelper;
import com.vise.basebluetooth.callback.IChatCallback;
import com.vise.basebluetooth.common.BleState;
import com.vise.basebluetooth.mode.BaseMessage;
import com.vise.basebluetooth.utils.HexUtil;
import com.vise.bluetoothchat.common.AppConstant;
import com.vise.bluetoothchat.mode.FriendInfo;
import com.vise.common_base.utils.ToastUtil;
import com.vise.common_utils.log.LogUtils;
import com.zz.squarebrick.GameApplication;
import com.zz.squarebrick.R;
import com.zz.squarebrick.game.Actions;
import com.zz.squarebrick.game.GameMsg;

import java.io.UnsupportedEncodingException;

public class GameRoomActivity extends AppCompatActivity {

    public static GameRoomActivity activity;
    private FriendInfo mFriendInfo;
    private BluetoothChatHelper mBluetoothChatHelper;
    private TextView tv_player1;
    private TextView tv_player2;
    private boolean isConnect;
    private Button btn_start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game_room);
        mBluetoothChatHelper = new BluetoothChatHelper(chatCallback);
        activity = this;
        initView();
        initData();
    }

    public BluetoothChatHelper getmBluetoothChatHelper() {
        return mBluetoothChatHelper;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBluetoothChatHelper != null && mFriendInfo == null) {//如果是房主则接受连接
            // Only if the state is STATE_NONE, do we know that we haven't
            // started already
            if (mBluetoothChatHelper.getState() == BleState.STATE_NONE) {
                // Start the Bluetooth chat services
                mBluetoothChatHelper.start(true);
            }
        }
    }

    private IChatCallback<byte[]> chatCallback = new IChatCallback<byte[]>() {
        @Override
        public void connectStateChange(BleState bleState) {
            LogUtils.i("connectStateChange:" + bleState.getCode());
            if (bleState == BleState.STATE_CONNECTED) {
                isConnect = true;
                ToastUtil.showToast(GameRoomActivity.this, getString(R.string.connect_friend_success));
            } else if (bleState == BleState.STATE_NONE) {
                isConnect = false;
                tv_player1.setText("未连接");
            }
        }

        @Override
        public void writeData(byte[] data, int type) {
            if (data == null) {
                LogUtils.e("writeData is Null or Empty!");
                return;
            }
            LogUtils.i("writeData:" + HexUtil.encodeHexStr(data));
        }

        @Override
        public void readData(byte[] data, int type) {
            if (data == null) {
                LogUtils.e("readData is Null or Empty!");
                return;
            }
            try {
                BaseMessage message = CommandHelper.unpackData(data);
                LogUtils.i("message:" + message.getMsgContent());
                String msgContent = message.getMsgContent();
                Gson gson = new Gson();
                GameMsg gameMsg = gson.fromJson(msgContent, GameMsg.class);
                onReceive(gameMsg);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void setDeviceName(String name) {
            LogUtils.i("setDeviceName:" + name);
            if (!TextUtils.isEmpty(name)) {
                tv_player1.setText(name);
            } else if (mFriendInfo != null) {
                tv_player1.setText(mFriendInfo.getFriendNickName());
            }
        }

        @Override
        public void showMessage(String message, int code) {
            if (!isFinishing()) {
                return;
            }
            LogUtils.i("showMessage:" + message);
            ToastUtil.showToast(GameRoomActivity.this, getString(R.string.connect_friend_fail));
        }
    };

    private void initData() {
        mFriendInfo = this.getIntent().getParcelableExtra(AppConstant.FRIEND_INFO);
        if (mFriendInfo == null) {
            btn_start.setText("开始游戏");
            return;
        }
        btn_start.setText("待准备");
        btn_start.setSelected(false);
        if (mFriendInfo.isOnline()) {

        } else {

        }
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothChatHelper.connect(mFriendInfo.getBluetoothDevice(), true);
            }
        }, 3000);
    }

    private void initView() {
        btn_start = (Button) findViewById(R.id.btn_start);
        tv_player1 = (TextView) findViewById(R.id.tv_player1);
        tv_player2 = (TextView) findViewById(R.id.tv_player2);
        tv_player2.setText(mBluetoothChatHelper.getAdapter().getName());
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnect && mFriendInfo == null) {
                    GameMsg msg = new GameMsg();
                    msg.setAction(Actions.ACTION_START_GAME);
                    sendMessage(msg);
                }
                if (mFriendInfo != null) {
                    GameApplication.getApp().getSoundManager().buttonSound();
                    if (btn_start.isSelected()) {
                        btn_start.setText("待准备");
                        btn_start.setSelected(false);
                    } else {
                        btn_start.setText("准备就绪");
                        btn_start.setSelected(true);
                    }
                }
            }
        });
    }

    private void startGame() {
        startActivityForResult(new Intent(GameRoomActivity.this, OnlineGameActivity.class), 88);
    }

    private void onReceive(GameMsg msg) {
        switch (msg.getAction()) {
            case Actions.ACTION_START_GAME:
                if (btn_start.isSelected()) {
                    GameMsg send = new GameMsg();
                    send.setAction(Actions.ACTION_OTHER_PREPARED);
                    sendMessage(send);
                    startGame();
                } else {
                    GameMsg send = new GameMsg();
                    send.setAction(Actions.ACTION_OTHER_NO_PREPARE);
                    sendMessage(send);
                }
                break;
            case Actions.ACTION_OTHER_NO_PREPARE:
                ToastUtil.showToast(GameRoomActivity.this, "对方还未准备");
                break;
            case Actions.ACTION_OTHER_PREPARED:
                GameApplication.getApp().getSoundManager().buttonSound();
                startGame();
                break;
        }
    }

    private void sendMessage(GameMsg msg) {
        Gson gson = new Gson();
        try {
            mBluetoothChatHelper.write(CommandHelper.packMsg(gson.toJson(msg)));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBluetoothChatHelper != null) {
            mBluetoothChatHelper.stop();
            mBluetoothChatHelper = null;
        }
        activity = null;
    }
}
