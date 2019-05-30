package com.jwkj.smartlinkdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mediatek.elian.ElianNative;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;


public class AddDeviceActivity extends AppCompatActivity implements View.OnClickListener {
    private Context context;
    TextView tx_wifiName,tx_receive;
    Button bt_send,bt_stop;
    EditText et_pwd;
    ElianNative elain;
    String ssid;
    String pwd="";
    boolean isRegFilter=false;
    boolean is5GWifi=false;
    boolean isWifiEncrypt=false;
    public UDPHelper mHelper;
    WifiManager wifiManager;
    boolean isSend=false;
    String userId;
    /**
     * 是否以可分享的方式添加设备
     */
    boolean isShare = true;
    static {
        System.loadLibrary("elianjni");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        userId = getIntent().getStringExtra("USERID");
        context=this;
        initUI();
        currenWifi();
        regFilter();
        //监听UDP广播
        mHelper = new UDPHelper(context,9988);
        listen();
        mHelper.StartListen();
    }
    public void initUI(){
        tx_wifiName=(TextView)findViewById(R.id.tx_wifiName);
        tx_receive=(TextView)findViewById(R.id.tx_receive);
        et_pwd=(EditText)findViewById(R.id.et_pwd);
        bt_send=(Button)findViewById(R.id.bt_send);
        bt_stop=(Button)findViewById(R.id.bt_stop);
        bt_send.setOnClickListener(this);
        bt_stop.setOnClickListener(this);

    }
    public void regFilter(){
        IntentFilter filter=new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(br,filter);
        isRegFilter=true;
    }
    BroadcastReceiver br=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")){
                currenWifi();
            }
        }
    };
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_send){
            pwd=et_pwd.getText().toString().trim();
            if (!isWifiConnected()||ssid == null || ssid.equals("")||ssid.equals("<unknown ssid>")) {
                Toast.makeText(context,"请先将手机连接到WiFi",Toast.LENGTH_SHORT).show();
                return;
            }
            if(is5GWifi){
                Toast.makeText(context,"设备不支持5G网络",Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isWifiEncrypt) {
                if (TextUtils.isEmpty(pwd)) {
                    Toast.makeText(context,"请输入WiFi密码",Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            sendWifi();
            isSend=true;
            tx_receive.append("开始发包......\n");
        }else if(v.getId() == R.id.bt_stop){
            if(!isSend){
                return;
            }
            stopSendWifi();
            tx_receive.append("停止发包\n");
            isSend=false;
        }
//        switch (v.getId()){
//            case R.id.bt_send:
//
//                break;
//            case R.id.bt_stop:
//
//                break;
//        }
    }
    //开始发包
    private void sendWifi(){
        if (elain == null) {
            elain = new ElianNative();
        }
        if (null != ssid && !"".equals(ssid)) {
            elain.InitSmartConnection(null, 1, 1);
            //wifi名  wifi密码  加密方式
            elain.StartSmartConnection(ssid, pwd, "", mAuthMode);
        }
    }
    //停止发包
    private void stopSendWifi() {
        if (elain != null) {
            elain.StopSmartConnection();
        }
    }
    void listen() {
        mHelper.setCallBack(new Handler() {

            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                switch (msg.what) {
                    case UDPHelper.HANDLER_MESSAGE_BIND_ERROR:
                        Log.e("my", "HANDLER_MESSAGE_BIND_ERROR");
                        break;
                    case UDPHelper.HANDLER_MESSAGE_RECEIVE_MSG:
                        Bundle bundle = msg.getData();
                        String contactId = bundle.getString("contactId");//设备ID
                        String frag = bundle.getString("frag");//有无密码标记
                        String ipFlag = bundle.getString("ipFlag");
                        String ip = bundle.getString("ip");//id地址
                        int type = bundle.getInt("type", 0);//设备主类型
                        int subType = bundle.getInt("subType", -1);//设备子类型
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
                        try {
                            saveContact.ipadressAddress = InetAddress.getByName(ip);
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        }
                        mHelper.StopListen();
//                        Intent it = new Intent();
//                        it.setAction(Constants.Action.RADAR_SET_WIFI_SUCCESS);
//                        sendBroadcast(it);
                        Long currentTime = System.currentTimeMillis();
                        //一分钟内不再搜出AP的wifi设备
                        if (isShare) {
//                            add_device.putExtra("contact", saveContact);
//                            if (Integer.parseInt(frag) == 0) {
//                                add_device.putExtra("isCreatePassword", true);
//                            } else {
//                                add_device.putExtra("isCreatePassword", false);
//                            }
//                            add_device.putExtra("ipAddress", ip);
//                            add_device.putExtra("initPwd", initPwd);
//                            startActivity(add_device);
                            Intent i = new Intent();
                            i.setAction("GWELL_ADD_DEVICE_FOR_SHARE");
                            i.putExtra("contact", saveContact);
                            if (Integer.parseInt(frag) == 0) {
                                i.putExtra("isCreatePassword", true);
                            } else {
                                i.putExtra("isCreatePassword", false);
                            }
                            i.putExtra("ipAddress", ip);
                            i.putExtra("initPwd", initPwd);
                            sendBroadcast(i);
                        } else {
                            String deviceInfo="deviceId="+contactId+" deviceType="+type+" subType="+subType+" ip="+ip;
                            if (Integer.parseInt(frag) == 0) {
                                deviceInfo=deviceInfo+"无密码";
                            } else {
                                deviceInfo=deviceInfo+"有密码";
                            }
                            tx_receive.append(deviceInfo+"\n\n");
                        }
                        finish();
                        break;
                }
            }

        });
    }
    private byte mAuthMode;
    private byte AuthModeAutoSwitch = 2;
    private byte AuthModeOpen = 0;
    private byte AuthModeShared = 1;
    private byte AuthModeWPA = 3;
    private byte AuthModeWPA1PSKWPA2PSK = 9;
    private byte AuthModeWPA1WPA2 = 8;
    private byte AuthModeWPA2 = 6;
    private byte AuthModeWPA2PSK = 7;
    private byte AuthModeWPANone = 5;
    private byte AuthModeWPAPSK = 4;
    //获取当前连接wifi
    public void currenWifi(){
        WifiInfo wifiInfo = getConnectWifiInfo();
        if (wifiInfo == null) {
            ssid="";
            tx_wifiName.setText("请先连接wifi");
            return;
        }
        ssid = wifiInfo.getSSID();
        if (ssid == null) {
            return;
        }
        if (ssid.equals("")) {
            return;
        }
        if (ssid.length() <= 0) {
            return;
        }
        int a = ssid.charAt(0);
        if (a == 34) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        if (!ssid.equals("<unknown ssid>") && !ssid.equals("0x")) {
            tx_wifiName.setText(ssid);
        }
        List<ScanResult> wifiList = getLists(context);
        if (wifiList == null) {
            return;
        }
        for (int i = 0; i < wifiList.size(); i++) {
            ScanResult result = wifiList.get(i);
            if (!result.SSID.equals(ssid)) {
                continue;
            }
            is5GWifi=is5GWifi(result.frequency);
            isWifiEncrypt=isWifiEncrypt(result);
            boolean bool1, bool2, bool3, bool4;
            bool1 = result.capabilities.contains("WPA-PSK");
            bool2 = result.capabilities.contains("WPA2-PSK");
            bool3 = result.capabilities.contains("WPA-EAP");
            bool4 = result.capabilities.contains("WPA2-EAP");
            if (result.capabilities.contains("WEP")) {
                this.mAuthMode = this.AuthModeOpen;
            }
            if ((bool1) && (bool2)) {
                mAuthMode = AuthModeWPA1PSKWPA2PSK;
            } else if (bool2) {
                this.mAuthMode = this.AuthModeWPA2PSK;
            } else if (bool1) {
                this.mAuthMode = this.AuthModeWPAPSK;
            } else if ((bool3) && (bool4)) {
                this.mAuthMode = this.AuthModeWPA1WPA2;
            } else if (bool4) {
                this.mAuthMode = this.AuthModeWPA2;
            } else {
                if (!bool3)
                    break;
                this.mAuthMode = this.AuthModeWPA;
            }
        }
    }
    //判断是否连接上wifi
    public  boolean isWifiConnected(){
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(wifiNetworkInfo.isConnected()){
            return true ;
        }
        return false ;
    }
    //获取当前连接wifi的WifiInfo
    public WifiInfo getConnectWifiInfo(){
        if(!isWifiConnected()){
            return null;
        }
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        if(wifiManager==null){
            return null;
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo;
    }
    //获取wifi列表
    public List<ScanResult> getLists(Context context) {
        wifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        wifiManager.startScan();
        List<ScanResult> lists = wifiManager.getScanResults();
        return lists;
    }
    //判断是不是5Gwifi
    public static boolean is5GWifi(int frequency){
        String str=String.valueOf(frequency);
        if(str.length()>0){
            char a=str.charAt(0);
            if(a=='5'){
                return true;
            }
        }
        return false;
    }

    //WiFi是否加密
    public static boolean isWifiEncrypt(ScanResult result) {
        return !(result.capabilities.toLowerCase().indexOf("wep") != -1
                || result.capabilities.toLowerCase().indexOf("wpa") != -1);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isSend){
            stopSendWifi();
            isSend=false;
        }
        if(isRegFilter){
            unregisterReceiver(br);
            isRegFilter=false;
        }
        if(mHelper!=null){
            mHelper.StopListen();
        }
    }
}
