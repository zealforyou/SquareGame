package com.zz.squarebrick.online;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.vise.basebluetooth.BluetoothChatHelper;
import com.vise.basebluetooth.CommandHelper;
import com.vise.basebluetooth.callback.IChatCallback;
import com.vise.basebluetooth.common.BleState;
import com.vise.basebluetooth.mode.BaseMessage;
import com.vise.basebluetooth.utils.HexUtil;
import com.vise.bluetoothchat.common.AppConstant;
import com.vise.bluetoothchat.mode.ChatInfo;
import com.vise.bluetoothchat.mode.FriendInfo;
import com.vise.common_base.utils.ToastUtil;
import com.vise.common_utils.log.LogUtils;
import com.vise.common_utils.utils.character.DateTime;
import com.zz.squarebrick.GameApplication;
import com.zz.squarebrick.R;

import java.io.UnsupportedEncodingException;
import java.util.Date;

public class GameRoomActivity extends AppCompatActivity {

    public static GameRoomActivity activity;
    private FriendInfo mFriendInfo;
    private BluetoothChatHelper mBluetoothChatHelper;
    private TextView tv_player1;
    private TextView tv_player2;
    private boolean isConnect;

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
        if (mBluetoothChatHelper != null) {
            // Only if the state is STATE_NONE, do we know that we haven't
            // started already
            if (mBluetoothChatHelper.getState() == BleState.STATE_NONE) {
                // Start the Bluetooth chat services
                mBluetoothChatHelper.start(false);
            }
        }
    }

    private IChatCallback<byte[]> chatCallback = new IChatCallback<byte[]>() {
        @Override
        public void connectStateChange(BleState bleState) {
            LogUtils.i("connectStateChange:" + bleState.getCode());
            if (bleState == BleState.STATE_CONNECTED) {
                isConnect = true;
                if (mFriendInfo != null) {
                    tv_player1.setText(mFriendInfo.getFriendNickName());
                }
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
            LogUtils.i("readData:" + HexUtil.encodeHexStr(data));
            try {
                BaseMessage message = CommandHelper.unpackData(data);
                ChatInfo chatInfo = new ChatInfo();
                chatInfo.setMessage(message);
                chatInfo.setReceiveTime(DateTime.getStringByFormat(new Date(), DateTime.DEFYMDHMS));
                chatInfo.setSend(false);
                chatInfo.setFriendInfo(mFriendInfo);
                LogUtils.i("message:" + message.getMsgContent());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void setDeviceName(String name) {
            LogUtils.i("setDeviceName:" + name);
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
            finish();
            return;
        }
        if (mFriendInfo.isOnline()) {

        } else {

        }
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothChatHelper.connect(mFriendInfo.getBluetoothDevice(), false);
            }
        }, 3000);
    }

    private void initView() {
        View btn_start = findViewById(R.id.btn_start);
        tv_player1 = (TextView) findViewById(R.id.tv_player1);
        tv_player2 = (TextView) findViewById(R.id.tv_player2);
        tv_player2.setText(mBluetoothChatHelper.getAdapter().getName());
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnect) {
                    GameApplication.getApp().getSoundManager().buttonSound();
//                    sendMessage();
                    startActivityForResult(new Intent(GameRoomActivity.this, OnlineGameActivity.class), 88);
                }
            }
        });
    }

    private void sendMessage() {
        try {
            mBluetoothChatHelper.write(CommandHelper.packMsg(""));
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
