package com.example.dansesshou.jcentertest;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gwelldemo.R;

import java.util.List;

import Utils.Contants;
import Utils.Util;
import Utils.WifiUtils;
import entity.WifiInformation;

/**
 * Created by lele on 2018/5/30.
 */

public class SmartLinkConfigWifiActivity extends AppCompatActivity implements View.OnClickListener {
    private Context context;
    private TextView tx_wifiname;
    private EditText et_pwd;
    private Button next;
    /**
     * wifi名
     */
    String ssid;
    /**h1
     * wifi是否加密
     */
    boolean isWifiEncrypt;
    /**
     * wifi加密类型
     */
    int type;
    /**
     * 是否5G网络
     */
    boolean is5GWifi;
    /**
     * ip地址
     */
    int mLocalIp;
    /**
     * mac地址
     */
    String mac;

    /*区分声波 二维码还是SimpleConfig*/
    private int subEncryptType;

    private String visitorUserPwd;
    public boolean isInitEMTMFSDK = false;
    private String deviceId;
    private int connectType;
    boolean isRegFilter = false;
    /*清除WIFI密码 扫描连接超时*/
    private boolean clearPwd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_config_wifi);
        visitorUserPwd = getIntent().getStringExtra("visitorUserPwd");
        deviceId = getIntent().getStringExtra("deviceId");
        connectType = getIntent().getIntExtra("connectType", Contants.ConnectWifiType.SCAN);
        clearPwd = getIntent().getBooleanExtra("clearPwd", false);
        initUI();
        regFilter();
    }

    private void initUI() {
        tx_wifiname = (TextView) findViewById(R.id.tx_wifiname);
        et_pwd = (EditText) findViewById(R.id.et_pwd);
        next = (Button) findViewById(R.id.next);
        next.setOnClickListener(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        visitorUserPwd = intent.getStringExtra("visitorUserPwd");
        deviceId = intent.getStringExtra("deviceId");
        connectType = intent.getIntExtra("connectType", Contants.ConnectWifiType.SCAN);
        clearPwd = intent.getBooleanExtra("clearPwd", false);
    }

    private void regFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Contants.Action.ACTION_NETWORK_CHANGE);
        registerReceiver(br, filter);
        isRegFilter = true;
    }

    BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Contants.Action.ACTION_NETWORK_CHANGE)) {
                try {
                    currentWifi();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    };

    public void currentWifi() {
        WifiInfo wifiInfo = WifiUtils.getInstance().getConnectWifiInfo();
        if (wifiInfo == null || TextUtils.isEmpty(wifiInfo.getSSID())) {
            tx_wifiname.setText("请将手机连接到WIFI");
            return;
        }
        ssid = wifiInfo.getSSID();
        mLocalIp = wifiInfo.getIpAddress();
        mac = wifiInfo.getMacAddress();
        int a = ssid.charAt(0);
        if (a == 34 && ssid.length() >= 3) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        if (ssid.equals("<unknown ssid>") || ssid.equals("0x")) {
            tx_wifiname.setText("请确认手机定位权限是否打开");
            return;
        }
        tx_wifiname.setText(ssid);
        List<ScanResult> wifiList = WifiUtils.getInstance().getLists();
        if (wifiList == null || 0 == wifiList.size()) {
            Toast.makeText(this, "请打开定位权限", Toast.LENGTH_LONG).show();
            return;
        }
        for (int i = 0; i < wifiList.size(); i++) {
            ScanResult result = wifiList.get(i);
            if (!result.SSID.equals(ssid)) {
                continue;
            }
            isWifiEncrypt = Util.isWifiOpen(result);
            if (isWifiEncrypt) {
                et_pwd.setVisibility(View.GONE);
            } else {
                et_pwd.setVisibility(View.VISIBLE);
            }
            is5GWifi = Util.is5GWifi(result.frequency);
            boolean bool1 = result.capabilities.contains("WPA-PSK");
            boolean bool2 = result.capabilities.contains("WPA2-PSK");
            boolean bool3 = result.capabilities.contains("WPA-EAP");
            boolean bool4 = result.capabilities.contains("WPA2-EAP");

            /*用于AP 二维码*/
            if (result.capabilities.indexOf("WPA") > 0) {
                type = 2;
            } else if (result.capabilities.indexOf("WEP") > 0) {
                type = 1;
            } else {
                type = 0;
            }

            /*用于SimpleConfig*/
            if (result.capabilities.contains("WEP")) {
                subEncryptType = 0;
            }
            if ((bool1) && (bool2)) {
                subEncryptType = 9;
            } else if (bool2) {
                subEncryptType = 7;
            } else if (bool1) {
                subEncryptType = 4;
            } else if ((bool3) && (bool4)) {
                subEncryptType = 8;
            } else if (bool4) {
                subEncryptType = 6;
            } else {
                if (!bool3) {
                    break;
                }
                subEncryptType = 3;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentWifi();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.next:
                String wifiPwd = et_pwd.getText().toString();
                String wifiName = tx_wifiname.getText().toString();
                if (!WifiUtils.isWifiConnected(context)) {
                    Toast.makeText(context, "请将手机连接到WIFI", Toast.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(wifiName)) {
                    Toast.makeText(context, "选择WIFI", Toast.LENGTH_LONG).show();
                    return;
                }
                if (is5GWifi) {
                    Toast.makeText(context, "不支持5G", Toast.LENGTH_LONG).show();
                    return;
                }
                if (!isWifiEncrypt) {
                    if (TextUtils.isEmpty(wifiPwd)) {
                        Toast.makeText(context, "请输入密码", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                if (isWifiEncrypt){
                    // 开放型WIFI
                    init(wifiName, "");
                }else {
                    init(wifiName,wifiPwd);
                }
                break;
            default:
                break;
        }
    }

    private void init(String wifiName,String wifiPwd){
            WifiInformation wifiInformation = new WifiInformation(wifiName, wifiPwd, type, mLocalIp, mac, subEncryptType);
            if (connectType == Contants.ConnectWifiType.SCAN ){
                Intent redeady = new Intent(context, SendSoundWaveGuideActivity.class);
                redeady.putExtra("connectType", connectType);
                redeady.putExtra("visitorUserPwd", visitorUserPwd);
                redeady.putExtra("WifiInformation", wifiInformation);
                redeady.putExtra("deviceId", deviceId);
                startActivity(redeady);
            }
    }

    @Override
    protected void onDestroy() {
        if (isRegFilter) {
            unregisterReceiver(br);
            isRegFilter = false;
        }
        super.onDestroy();
    }
}
