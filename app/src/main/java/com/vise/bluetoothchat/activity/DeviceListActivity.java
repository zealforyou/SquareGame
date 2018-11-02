package com.vise.bluetoothchat.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.vise.basebluetooth.utils.BluetoothUtil;
import com.vise.bluetoothchat.adapter.GroupFriendAdapter;
import com.vise.bluetoothchat.common.AppConstant;
import com.vise.bluetoothchat.mode.FriendInfo;
import com.vise.bluetoothchat.mode.GroupInfo;
import com.vise.common_base.manager.AppManager;
import com.vise.common_base.utils.ToastUtil;
import com.vise.common_utils.utils.character.DateTime;
import com.vise.common_utils.utils.view.ActivityUtil;
import com.zz.squarebrick.GameApplication;
import com.zz.squarebrick.R;
import com.zz.squarebrick.online.GameRoomActivity;
import com.zz.squarebrick.outline.GamePrepareActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class DeviceListActivity extends BaseChatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ExpandableListView mGroupFriendLv;
    private GroupFriendAdapter mGroupFriendAdapter;
    private List<GroupInfo> mGroupFriendListData = new ArrayList<>();
    private BroadcastReceiver bluetoothReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_friend);
    }

    @Override
    protected void initWidget() {

        ImageView fab = (ImageView) findViewById(R.id.fab);
        ImageView create_room = (ImageView) findViewById(R.id.create_room);
        create_room.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeviceListActivity.this, GameRoomActivity.class);
                intent.putExtra("create", true);
                startActivity(intent);
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityUtil.startForwardActivity(DeviceListActivity.this, AddFriendActivity.class);
            }
        });


        mGroupFriendLv = (ExpandableListView) findViewById(R.id.friend_group_list);
    }

    @Override
    protected void initData() {
        mGroupFriendAdapter = new GroupFriendAdapter(mContext, mGroupFriendListData);
        mGroupFriendLv.setAdapter(mGroupFriendAdapter);
        mGroupFriendLv.expandGroup(0);

        if (BluetoothUtil.isSupportBle(mContext)) {
            BluetoothUtil.enableBluetooth((Activity) mContext, 1);
        } else {
            ToastUtil.showToast(mContext, getString(R.string.phone_not_support_bluetooth));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    AppManager.getAppManager().appExit(mContext);
                }
            }, 3000);
        }

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        bluetoothReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int boundState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
                if (boundState == BluetoothDevice.BOND_BONDED) {
                    findDevice();
                }

            }
        };
        registerReceiver(bluetoothReceiver, filter);
    }

    @Override
    protected void initEvent() {
        mGroupFriendLv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                FriendInfo friendInfo = mGroupFriendListData.get(groupPosition).getFriendList().get(childPosition);
                Bundle bundle = new Bundle();
                bundle.putParcelable(AppConstant.FRIEND_INFO, friendInfo);
                ActivityUtil.startForwardActivity(DeviceListActivity.this, GameRoomActivity.class, bundle, false);
                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                findDevice();
            } else {
                AppManager.getAppManager().appExit(mContext);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onRestart() {
        if (BluetoothUtil.isSupportBle(mContext)) {
            BluetoothUtil.enableBluetooth((Activity) mContext, 1);
        } else {
            ToastUtil.showToast(mContext, getString(R.string.phone_not_support_bluetooth));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    AppManager.getAppManager().appExit(mContext);
                }
            }, 3000);
        }
        super.onRestart();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_about) {
            displayAboutDialog();
            return true;
        } else if (id == R.id.menu_share) {
            ActivityUtil.startForwardActivity(this, GamePrepareActivity.class);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }
        return true;
    }

    private void displayAboutDialog() {
        final int paddingSizeDp = 5;
        final float scale = getResources().getDisplayMetrics().density;
        final int dpAsPixels = (int) (paddingSizeDp * scale + 0.5f);

        final TextView textView = new TextView(this);
        final SpannableString text = new SpannableString(getString(R.string.about_dialog_text));

        textView.setText(text);
        textView.setAutoLinkMask(RESULT_OK);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setPadding(dpAsPixels, dpAsPixels, dpAsPixels, dpAsPixels);

        Linkify.addLinks(text, Linkify.ALL);
        new AlertDialog.Builder(this)
                .setTitle(R.string.menu_about)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, null)
                .setView(textView)
                .show();
    }

    private void findDevice() {
        // 获得已经保存的配对设备
        Set<BluetoothDevice> pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        if (pairedDevices.size() > 0) {
            mGroupFriendListData.clear();
            GroupInfo groupInfo = new GroupInfo();
            groupInfo.setGroupName(BluetoothAdapter.getDefaultAdapter().getName());
            List<FriendInfo> friendInfoList = new ArrayList<>();
            for (BluetoothDevice device : pairedDevices) {
                FriendInfo friendInfo = new FriendInfo();
                friendInfo.setIdentificationName(device.getName());
                friendInfo.setDeviceAddress(device.getAddress());
                friendInfo.setFriendNickName(device.getName());
                friendInfo.setOnline(false);
                friendInfo.setJoinTime(DateTime.getStringByFormat(new Date(), DateTime.DEFYMDHMS));
                friendInfo.setBluetoothDevice(device);
                friendInfoList.add(friendInfo);
                friendInfo.setAvatarId((int) (Math.random() * 14 * 16));
            }
            groupInfo.setFriendList(friendInfoList);
            groupInfo.setOnlineNumber(0);
            mGroupFriendListData.add(groupInfo);
            mGroupFriendAdapter.setGroupInfoList(mGroupFriendListData);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothReceiver != null) unregisterReceiver(bluetoothReceiver);
        GameApplication.getApp().getSoundManager().release();
        GameApplication.getApp().setSoundManager(null);
    }
}
