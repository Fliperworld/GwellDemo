package com.example.dansesshou.jcentertest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gwelldemo.R;
import com.jwkj.smartlinkdemo.AddDeviceActivity;
import com.p2p.core.P2PHandler;
import com.p2p.core.utils.SharedPrefreUtils;

import Utils.Contants;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import service.MainService;

public class MainActivity extends BaseActivity {

    @BindView(R.id.btn_play_back)
    Button btnPlayBack;
    @BindView(R.id.btn_getalarm_picture)
    Button btnGetalarmPicture;
    @BindView(R.id.tx_alert)
    TextView txAlert;
    @BindView(R.id.sensor)
    Button btnSensor;
    @BindView(R.id.btn_alarmlist)
    Button btnAlarmlist;
    @BindView(R.id.btn_serialapp)
    Button btnSerialapp;
    @BindView(R.id.activity_main)
    LinearLayout activityMain;
    @BindView(R.id.btn_alarm_email)
    Button btnAlarmEmail;
    @BindView(R.id.btn_setting)
    Button btnSetting;
    @BindView(R.id.btn_aliplay)
    Button btnAliplay;
    private Context mContext;
    String userId;
    @BindView(R.id.btn_test)
    Button btnIn;
    @BindView(R.id.btn_moniter)
    Button btnMoniter;
    @BindView(R.id.btn_panorma)
    Button btnPanoMoniter;
    @BindView(R.id.btn_add_device)
    Button btnAddDevice;
    @BindView(R.id.btn_share_device)
    Button btnShareDevice;
    @BindView(R.id.btn_permission)
    Button btnPermission;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        userId = getIntent().getStringExtra(LoginActivity.USERID);
        SharedPrefreUtils.getInstance().putStringData(this, Contants.USERID, userId);
        initUI();
        initData();
        Intent intent = new Intent(this, MainService.class);
        startService(intent);
        registReg();
    }

    private void registReg() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Contants.P2P_CONNECT);
        filter.addAction("GWELL_ADD_DEVICE_FOR_SHARE");
        registerReceiver(receiver, filter);
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Contants.P2P_CONNECT.equals(intent.getAction())) {
                boolean connect = intent.getBooleanExtra("connect", false);
                //p2p连接失败  相应处理，用户可以根据具体情况自定义
                if (!connect) {
                    Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                    txAlert.setVisibility(View.VISIBLE);
                    btnPlayBack.setEnabled(false);
                    btnGetalarmPicture.setEnabled(false);
                    btnIn.setEnabled(false);
                    btnMoniter.setEnabled(false);
                    btnSensor.setEnabled(false);
                    btnSerialapp.setEnabled(false);
                    btnAlarmEmail.setEnabled(false);
                    btnAlarmlist.setEnabled(false);
                    btnSetting.setEnabled(false);
                    btnPanoMoniter.setEnabled(false);
                }
            } else if ("GWELL_ADD_DEVICE_FOR_SHARE".equals(intent.getAction())) {
                Intent configDevice = new Intent(mContext, ConfigurationDeviceActivity.class);
                configDevice.putExtra(LoginActivity.USERID, userId);
                configDevice.putExtra("contact", intent.getSerializableExtra("contact"));
                configDevice.putExtra("isCreatePassword", intent.getBooleanExtra("isCreatePassword",false));
                configDevice.putExtra("ipAddress", intent.getStringExtra("ipAddress"));
                configDevice.putExtra("initPwd", intent.getStringExtra("initPwd"));
                startActivity(configDevice);
            }

        }
    };

    private void initUI() {

    }

    private void initData() {
        String[] name = new String[]{"1092482"};
        P2PHandler.getInstance().getFriendStatus(name, 1);
    }

    @OnClick(R.id.btn_test)
    public void toDeviceTest() {
        Log.e("dxsTest", "toDeviceTest" + userId);
        startActivity(new Intent(this, DeviceTestActivity.class));
    }

    @OnClick(R.id.btn_moniter)
    public void toMoniter() {
        Intent moniter = new Intent(this, MonitorActivity.class);
        moniter.putExtra(LoginActivity.USERID, userId);
        startActivity(moniter);
        Log.e("dxsTest", "toMoniter" + userId);
    }

    @OnClick(R.id.btn_play_back)
    public void onClick() {
        Intent record = new Intent(this, RecordFilesActivity.class);
        record.putExtra(LoginActivity.USERID, userId);
        startActivity(record);
    }

    @OnClick(R.id.btn_serialapp)
    public void onSerialApp() {
        Intent serialapp = new Intent(this, SerialAppActivity.class);
//        record.putExtra(LoginActivity.USERID, userId);
        startActivity(serialapp);
    }

    @OnClick(R.id.btn_getalarm_picture)
    public void GetAllarmImage() {
        Intent record = new Intent(this, AllarmImageActivity.class);
        record.putExtra(LoginActivity.USERID, userId);
        startActivity(record);
    }

    @OnClick(R.id.btn_alarmlist)
    public void AlarmList() {
        Intent record = new Intent(this, AllarmImageListActivity.class);
        record.putExtra(LoginActivity.USERID, userId);
        startActivity(record);
    }

    @OnClick(R.id.sensor)
    public void onClicksensor() {
        Intent sensor = new Intent(this, SensorActivity.class);
        sensor.putExtra(LoginActivity.USERID, userId);
        startActivity(sensor);
    }

    @OnClick(R.id.btn_alarm_email)
    public void AlarmEmail() {
        Intent alarmEmail = new Intent(this, AlarmEmailActivity.class);
        startActivity(alarmEmail);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //此处disconnect是demo写法,正式工程只需在app结束时调用一次,与connect配对使用
        P2PHandler.getInstance().p2pDisconnect();
        unregisterReceiver(receiver);
    }

    @OnClick(R.id.btn_setting)
    public void onSetting() {
        Intent setting = new Intent(this, SettingActivity.class);
        startActivity(setting);
    }

    @OnClick(R.id.btn_aliplay)
    public void onAliPlay() {
        Intent setting = new Intent(this, AliPlayActivity.class);
        startActivity(setting);
    }

    @OnClick(R.id.btn_panorma)
    public void onPanMoni() {
        Intent setting = new Intent(this, ContactInfoActivity.class);
        startActivity(setting);
    }

    @OnClick(R.id.btn_add_device)
    public void onAddDevice() {
        Intent intent = new Intent(this, ChooseAddWayActivity.class);
        intent.putExtra(LoginActivity.USERID, userId);
        startActivity(intent);
    }

    @OnClick(R.id.btn_share_device)
    public void onShareDevice() {
        Intent addDevice = new Intent(this, ShareDeviceActivity.class);
        addDevice.putExtra(LoginActivity.USERID, userId);
        startActivity(addDevice);
    }

    @OnClick(R.id.btn_permission)
    public void onPermissionManage() {
        Intent addDevice = new Intent(this, PermissionManageActivity.class);
        addDevice.putExtra(LoginActivity.USERID, userId);
        startActivity(addDevice);
    }



}
