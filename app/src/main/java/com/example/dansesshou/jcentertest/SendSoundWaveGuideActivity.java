package com.example.dansesshou.jcentertest;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.gwelldemo.R;
import com.jwkj.smartlinkdemo.UDPHelper;
import com.jwsd.libzxing.QRCodeManager;
import com.p2p.core.P2PHandler;
import com.p2p.core.P2PValue;
import com.p2p.core.utils.SharedPrefreUtils;

import Utils.Contants;
import Utils.QrCodeFormatUtil;
import Utils.Util;
import entity.Contact;
import entity.WifiInformation;
import sdk.MyApp;

public class SendSoundWaveGuideActivity extends AppCompatActivity {
    private Context mContext;
    boolean isRegFilter = false;
    private String visitorUserPwd;
    private WifiInformation wifiInformation;
    private UDPHelper mHelper;
    private String deviceId;
    private int connectType;
    private ImageView iv_qrcode;
    private Bitmap bitmap;
    private Handler checkOnlineHandler = new Handler();
    private static final long CHECK_ONLINE_INTERVAL = 3 * 1000;
    private boolean isCheckedOnlineStatus = false;
    private String userId;

    @Override
    protected void onCreate(Bundle arg0) {
        // TODO Auto-generated method stub
        super.onCreate(arg0);
        setContentView(R.layout.activity_send_sound_wave);
        mContext = this;
        userId = SharedPrefreUtils.getInstance().getStringData(this, Contants.USERID);
        connectType = getIntent().getIntExtra("connectType", Contants.ConnectWifiType.SCAN);
        visitorUserPwd = getIntent().getStringExtra("visitorUserPwd");
        wifiInformation = (WifiInformation) getIntent().getSerializableExtra("WifiInformation");
        deviceId = getIntent().getStringExtra("deviceId");
        regFilter();
        mHelper = new UDPHelper(MyApp.app, 9988);
        listen();
        // 每隔三秒查询一次在线状态 用于处理AP隔离的路由器
        checkOnlineHandler.postDelayed(checkDeviceOnlineRunnable, CHECK_ONLINE_INTERVAL);
        initComponent();
    }

    public void initComponent() {
        iv_qrcode = (ImageView) findViewById(R.id.iv_qrcode);
        if (connectType == Contants.ConnectWifiType.SCAN){
            //生成二维码
            String qrInfo = "0"+ QrCodeFormatUtil.toHexString(wifiInformation.getWifiName().getBytes().length) + wifiInformation.getWifiName()
                    + "1" + QrCodeFormatUtil.toHexString(wifiInformation.getWifiPwd().getBytes().length) + wifiInformation.getWifiPwd()
                    + "2" + "01" + wifiInformation.getEncryptType()
                    + "3" + QrCodeFormatUtil.toHexString(Integer.toHexString((Integer.parseInt(userId) | 0x80000000)).length())
                    + Integer.toHexString((Integer.parseInt(userId) | 0x80000000));
            Log.i("SendSoundWave", "qrInfo = " + qrInfo + ", wifi = " + wifiInformation.getWifiName() + ", pwd = " + wifiInformation.getWifiPwd() + ", pwd length = "
                    + QrCodeFormatUtil.toHexString(wifiInformation.getWifiPwd().getBytes().length)
                    + ", encrypt = " + wifiInformation.getEncryptType() + ", num = " + userId + "," + Integer.toHexString((Integer.parseInt(userId) | 0x80000000)));
            int width = Util.dip2px(mContext,  280);
            bitmap = QRCodeManager.getInstance().createQRCode(qrInfo, width, width);
            iv_qrcode.setImageBitmap(bitmap);
            connectDeviceWithScan();
        }
    }

    private void connectDeviceWithScan(){
        mHelper.StartListen();
    }

    public void regFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Contants.Action.GET_FRIENDS_STATE);
        registerReceiver(br, filter);
        isRegFilter = true;
    }

    private Runnable checkDeviceOnlineRunnable = new Runnable() {
        @Override
        public void run() {
            if (deviceId != null) {
                String[] contactIds = {deviceId};
                P2PHandler.getInstance().getFriendStatus(contactIds, 2);
            }
            if (checkOnlineHandler != null) {
                checkOnlineHandler.postDelayed(checkDeviceOnlineRunnable, CHECK_ONLINE_INTERVAL);
            }
        }
    };

    BroadcastReceiver br = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            Log.e("SendSoundWave", "intent = " + intent.getAction());
            if (intent.getAction().equals(Contants.Action.GET_FRIENDS_STATE)) {
                String contactIDs = intent.getStringExtra("contactIDs");
                int status = intent.getIntExtra("status", Contants.DeviceState.OFFLINE);
                if (TextUtils.isEmpty(contactIDs)) {
                    return;
                }
                Log.e("SendSoundWave", "contatId = " + contactIDs + ", deviceId = " + deviceId + ", status = " + status);
                if (contactIDs.equals(deviceId)) {
                    if (isCheckedOnlineStatus) {
                        return;
                    }
                    Log.e("SendSoundWave", "......" + status);
                    if (status == Contants.DeviceState.ONLINE) {
                        // 当设备处于在线状态的时候 开始进行
                        isCheckedOnlineStatus = true;
                        if (mHelper != null) {
                            mHelper.StopListen();
                        }
                        Contact saveContact = new Contact();
                        saveContact.contactId = deviceId;
                        Intent add_device = new Intent(mContext, ConfigurationDeviceActivity.class);
                        add_device.putExtra("contact", saveContact);
                        add_device.putExtra("visitorUserPwd", visitorUserPwd);
                        startActivity(add_device);
                        if (checkOnlineHandler != null) {
                            checkOnlineHandler.removeCallbacksAndMessages(null);
                            checkOnlineHandler = null;
                        }
                        finish();
                    }
                }
            }
        }
    };



    @Override
    protected void onDestroy() {
        if (isRegFilter) {
            unregisterReceiver(br);
            isRegFilter = false;
        }
        if (checkOnlineHandler != null) {
            checkOnlineHandler.removeCallbacksAndMessages(null);
            checkOnlineHandler = null;
        }
        super.onDestroy();
    }

    void listen() {
        mHelper.setCallBack(new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UDPHelper.HANDLER_MESSAGE_BIND_ERROR:
                        Log.e("my", "HANDLER_MESSAGE_BIND_ERROR");
                        break;
                    case UDPHelper.HANDLER_MESSAGE_RECEIVE_MSG:
                        isCheckedOnlineStatus = true;
                        Bundle bundle = msg.getData();
                        String contactId = bundle.getString("contactId");
                        Log.e("SendSoundWave", "deviceId = " + deviceId + ",contactId = " + contactId);
                        String frag = bundle.getString("frag");
                        String ipFlag = bundle.getString("ipFlag");
                        String ip = bundle.getString("ip");
                        int type = bundle.getInt("type", P2PValue.DeviceType.UNKNOWN);
                        int subType = bundle.getInt("subType", P2PValue.subType.UNKOWN);
                        int customId = bundle.getInt("customId");
                        String initPwd = bundle.getString("initPwd");
                        String mac = bundle.getString("mac");
                        Contact saveContact = new Contact();
                        saveContact.contactId = contactId;
                        saveContact.contactName = contactId;
                        saveContact.activeUser = userId;
                        saveContact.contactType = type;
                        saveContact.subType = subType;
                        saveContact.setCustomId(customId);
                        saveContact.setMac(mac);
                        Intent add_device = new Intent(mContext, ConfigurationDeviceActivity.class);
                        add_device.putExtra("contact", saveContact);
                        if (Integer.parseInt(frag) == Contants.DeviceFlag.UNSET_PASSWORD) {
                            add_device.putExtra("isCreatePassword", true);
                        } else {
                            add_device.putExtra("isCreatePassword", false);
                        }
                        add_device.putExtra("ipAddress", ip);
                        add_device.putExtra("visitorUserPwd", visitorUserPwd);
                        add_device.putExtra("initPwd", initPwd);
                        startActivity(add_device);
                        finish();
                        break;
                    default:
                        break;
                }
            }

        });
    }
}
