package com.example.dansesshou.jcentertest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gwelldemo.R;
import com.p2p.core.P2PHandler;
import com.p2p.core.global.SDKError;
import com.p2p.core.utils.MyUtils;

import Utils.Contants;
import entity.Contact;

/**
 * 智能联机添加设备时，如果有密码，进入该页面修改密码和设置访客密码
 * 修改密码为了增加成功率，发6次修改密码的命令，每隔5s发一次，IP和ID轮流发，直到设置成功；如果设置密码时，返回密码错误，用
 * 修改的密码去设置下访客密码，如果设置成功，认为密码已经修改成功，如果还是返回密码错误，提示密码错误（这样是为了解决，如果
 * 密码已经修改成功，但未返回，这个时候发修改密码的命令，就会密码错误的情况）
 *
 */

public class SetDevicePwdActivity extends BaseActivity implements View.OnClickListener {
    private Context context;
    private Contact contact;
    String visitorPwd, visitorUserPwd;
    boolean isRegFilter = false;
    private TextView tx_deviceId;
    private EditText et_pwd;
    private Button bt_confirm;
    private String password;
    private String ipAddress;
    //是否接收到设置设备密码成功返回
    private boolean isRecieve = false;
    private boolean isStopSend = false;
    /**
     * 尝试密码的次数(超过三次提示找不到密码或多次失败的弹框)
     */
    int verifyPwdCount = 0;
    TextView txt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_device_pwd);
        contact = (Contact) getIntent().getSerializableExtra("contact");
        visitorPwd = getIntent().getStringExtra("visitorPwd");
        visitorUserPwd = getIntent().getStringExtra("visitorUserPwd");
        ipAddress = getIntent().getStringExtra("ipAddress");

        context = this;
        initUI();
        regFilter();
    }

    private void initUI() {
        tx_deviceId = (TextView) findViewById(R.id.tx_deviceId);
        et_pwd = (EditText) findViewById(R.id.et_pwd);
        bt_confirm = (Button) findViewById(R.id.bt_confirm);
        txt = (TextView) findViewById(R.id.txt);

        tx_deviceId.setText( contact.contactId);
        bt_confirm.setOnClickListener(this);
        randomCreatePwd();
    }

    private void regFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Contants.P2P.ACK_RET_SET_DEVICE_PASSWORD);
        filter.addAction(Contants.P2P.RET_SET_DEVICE_PASSWORD);
        filter.addAction(Contants.P2P.ACK_RET_SET_VISITOR_DEVICE_PASSWORD);
        filter.addAction(Contants.P2P.RET_SET_VISITOR_DEVICE_PASSWORD);
        registerReceiver(br, filter);
        isRegFilter = true;
    }

    BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Contants.P2P.ACK_RET_SET_DEVICE_PASSWORD)) {
                //如果收到了设置密码的返回，不再处理ACK回调
                if (!isRecieve) {
                    int result = intent.getIntExtra("result", -1);
                    if (result == Contants.P2P_SET.ACK_RESULT.ACK_PWD_ERROR) {
                        //如果密码错误也去设置访客密码，如果设置成功认为修改管理密码成功了（防止已经修改成功，但未返回信息的情况）
                        isRecieve = true;
                        P2PHandler.getInstance().setDeviceVisitorPassword(contact.getRealContactID(),
                                contact.contactPassword, visitorPwd,contact.getDeviceIp());
                    } else if (result == Contants.P2P_SET.ACK_RESULT.ACK_NET_ERROR) {
                        if (!TextUtils.isEmpty(password)) {
                            P2PHandler.getInstance().setDevicePassword(contact.getRealContactID(),
                                    password,
                                    contact.contactPassword,
                                    contact.userPassword,
                                    contact.userPassword,
                                    contact.getDeviceIp());
                        }
                    } else if (result == Contants.P2P_SET.ACK_RESULT.ACK_INSUFFICIENT_PERMISSIONS) {
                        showInfo("权限不足");
                    }
                }
            } else if (intent.getAction().equals(Contants.P2P.RET_SET_DEVICE_PASSWORD)) {
                int result = intent.getIntExtra("result", -1);
                if (result == Contants.P2P_SET.DEVICE_PASSWORD_SET.SETTING_SUCCESS) {
                    mark = SDKError.ShareAdd.SET_DEVICE_VISITOR_PWD_ERROR;
                    isRecieve = true;
                    P2PHandler.getInstance().setDeviceVisitorPassword(contact.getRealContactID(), contact.contactPassword, visitorPwd, contact.getDeviceIp());
                }
            } else if (intent.getAction().equals(Contants.P2P.RET_SET_VISITOR_DEVICE_PASSWORD)) {
                int result = intent.getIntExtra("result", -1);
                if (result == Contants.P2P_SET.DEVICE_VISITOR_PASSWORD_SET.SETTING_SUCCESS) {
                    isRecieve = true;
                    isStopSend = true;
                    //设置访客密码成功，跳到绑定设备页面
                    Intent add_device = new Intent(context, ConfigurationDeviceActivity.class);
                    add_device.putExtra("contact", contact);
                    add_device.putExtra("isSetPwd", true);
                    add_device.putExtra("visitorUserPwd", visitorUserPwd);
                    add_device.putExtra("visitorPwd", visitorPwd);
                    startActivity(add_device);
                    finish();
                }
            } else if (intent.getAction().equals(Contants.P2P.ACK_RET_SET_VISITOR_DEVICE_PASSWORD)) {
                int result = intent.getIntExtra("state", -1);
                int msgId = intent.getIntExtra("msgId", 0);
                if (result == Contants.P2P_SET.ACK_RESULT.ACK_NET_ERROR) {
                    if (isStopSend) {
                        return;
                    }
                    //IP和ID轮流发（如果手机和设备连的不是同一个网络，只有ID号才能设置成功）
                        P2PHandler.getInstance().setDeviceVisitorPassword(contact.getRealContactID(), contact.contactPassword, visitorPwd,contact.getDeviceIp());
                } else if (result == Contants.P2P_SET.ACK_RESULT.ACK_PWD_ERROR) {
                   showInfo("密码错误，请重试！");
                }
            }
        }
    };

    private void showInfo(String text) {
        String info = txt.getText().toString().trim();

        txt.setText(info+"\n"+text);
    }

    //修改密码
    private void modifyPwd() {
        isRecieve = false;
        isStopSend = false;
        mark = SDKError.ShareAdd.MODIFY_DEVICE_PWD_ERROR;
        String pwd = et_pwd.getText().toString().trim();
        if (TextUtils.isEmpty(pwd)) {
            Toast.makeText(context, "不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        showInfo("修改密码...");
        password = P2PHandler.getInstance().EntryPassword(pwd);
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 6; i++) {
                    if (isRecieve) {
                        break;
                    } else {
                        P2PHandler.getInstance().setDevicePassword(contact.contactId, password, contact.contactPassword, contact.userPassword, contact.userPassword, 0);
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isRegFilter) {
            unregisterReceiver(br);
            isRegFilter = false;
        }
    }

    int mark = SDKError.ShareAdd.MODIFY_DEVICE_PWD_ERROR;//标记走到哪一步

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_confirm:
                modifyPwd();
                break;
            default:
                break;
        }
    }



    //随机产生管理密码和访客密码(如果密码为空，重新生成密码)
    private void randomCreatePwd() {
        if (TextUtils.isEmpty(contact.contactPassword)) {
            //随机产生管理密码
            String[] pwds = MyUtils.createRandomPassword(0);
            contact.userPassword = pwds[0];
            contact.contactPassword = pwds[1];
        }
        if (TextUtils.isEmpty(visitorPwd)) {
            //随机生成访客密码
            String[] vPwds = MyUtils.createRandomPassword(1);
            visitorUserPwd = vPwds[0];
            visitorPwd = vPwds[1];
        }
    }

}
