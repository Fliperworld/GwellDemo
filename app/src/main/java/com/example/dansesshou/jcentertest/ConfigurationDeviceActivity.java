package com.example.dansesshou.jcentertest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.gwelldemo.R;
import com.libhttp.entity.DeviceBindMasterResult;
import com.libhttp.entity.DeviceSync;
import com.libhttp.entity.LockDeviceBindMasterResult;
import com.libhttp.request.MasterDeviceSync;
import com.libhttp.subscribers.SubscriberListener;
import com.p2p.core.P2PHandler;
import com.p2p.core.P2PSpecial.HttpErrorCode;
import com.p2p.core.P2PSpecial.HttpSend;
import com.p2p.core.P2PValue;
import com.p2p.core.utils.MyUtils;

import Utils.Contants;
import Utils.Util;
import entity.Contact;
import entity.WifiInformation;

/**
 * <p>
 * 有三个地方会进入该页面：
 * 1.智能联机配网成功后，进入该页面。(按一下步骤进行，前一步成功才可进入下一步)
 * 1)锁定设备
 * 2)设置初始密码 如果有密码，用三种常用的密码（"123", "66666666", "123456"）尝试修改密码，如果失败，进入修改密码页面；如果没有密码，去设置初始密码
 * 3)设置访客密码
 * 4)绑定设备，绑定成功后可把设备添加到设备列表
 * <p>
 * 2.AP配网成功后，进入该页面。先去设置访客密码，设置成功后，绑定设备
 * <p>
 * 注意：
 * 为了提高成功率：
 * 锁定设备，设置初始密码，访客密码，绑定设备，每一个过程，可都给两次机会
 *
 * </p>
 */

public class ConfigurationDeviceActivity extends BaseActivity {
    private Context context;
    private Contact contact;
    //是否需要创建密码
    private boolean isCreatePassword;
    //ip地址
    private String ipAddress;
    //ip地址末段
    String ipFrag;
    boolean isRegFilter = false;
    ImageView back_btn;
    TextView tx_title;
    /**
     * 访客密码
     */
    String visitorPwd, visitorUserPwd;
    //是否AP配网连接
    boolean isApAdd = false;
    //设置初始密码超时时间
    private static final long SET_INITPWD_TIME_OUT = 40 * 1000;
    //设置访客密码超时时间
    private static final long SET_VISITORPWD_TIME_OUT = 40 * 1000;
    //修改密码超时时间
    private static final long MODIFY_PWD_TIME_OUT = 40 * 1000;
    //是否已经设置好密码
    boolean isSetPwd = false;
    private String initPwd;
    boolean isExit = false;

    private int sendVisitorTimes = 0;
    private String userId;

    TextView tx_receive;
    Button  btn;
    EditText et_pwd;
    private WifiInformation wifiInformation;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration_device);
        context = this;
        contact = (Contact) getIntent().getSerializableExtra("contact");
        isCreatePassword = getIntent().getBooleanExtra("isCreatePassword", false);
        ipAddress = getIntent().getStringExtra("ipAddress");
        initPwd = getIntent().getStringExtra("initPwd");
        userId = getIntent().getStringExtra(LoginActivity.USERID);
        isSetPwd = getIntent().getBooleanExtra("isSetPwd", false);
        visitorUserPwd = getIntent().getStringExtra("visitorUserPwd");
        visitorPwd = getIntent().getStringExtra("visitorPwd");
        wifiInformation = (WifiInformation) getIntent().getSerializableExtra("WifiInformation");
        if (!TextUtils.isEmpty(ipAddress)) {
            ipFrag = ipAddress.substring(ipAddress.lastIndexOf(".") + 1, ipAddress.length());
        }
        isSetPwd = getIntent().getBooleanExtra("isSetPwd", false);
        initUI();
        regFilter();
        addDevice();
    }

    private void initUI() {
//        back_btn = (ImageView) findViewById(R.id.back_btn);
//        tx_title = (TextView) findViewById(R.id.tx_title);
//        iv_two = (ImageView) findViewById(R.id.iv_two);
        et_pwd = (EditText) findViewById(R.id.et_pwd);
        btn = (Button) findViewById(R.id.btn);
//        back_btn.setOnClickListener(this);
        tx_receive = (TextView) findViewById(R.id.tx_receive);
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                modifyPassword();
//            }
//        });
    }



    private void addDevice() {
        //智能联机配网
        if (isSetPwd) {
            //已经设置好设备密码和访客密码，直接绑定（从验证密码页面进来）
            deviceBindMaster();
            setUIStep(2);
        }else {
            setUIStep(1);
            randomCreatePwd();
            if (TextUtils.isEmpty(visitorUserPwd)) {
                //未锁定设备
                lockDeviceBindMaster();
            } else {
                //已经锁定设备(扫描二维码添加，扫描的时候就已经锁定设备，获取到访客密码了)
                visitorPwd = P2PHandler.getInstance().EntryPassword(visitorUserPwd);
                if (isCreatePassword) {
                    //没有密码，设置初始密码
                    setInitPassword();
                } else {
                    //有默认密码，修改密码
                    modifyPwd();
                }
            }
        }

    }

    private void regFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Contants.P2P.RET_SET_INIT_PASSWORD);
        filter.addAction(Contants.P2P.ACK_RET_CHECK_PASSWORD);
        //设置访客密码
        filter.addAction(Contants.P2P.RET_SET_VISITOR_DEVICE_PASSWORD);
        filter.addAction(Contants.P2P.ACK_RET_SET_VISITOR_DEVICE_PASSWORD);
        //修改密码
        filter.addAction(Contants.P2P.ACK_RET_SET_DEVICE_PASSWORD);
        filter.addAction(Contants.P2P.RET_SET_DEVICE_PASSWORD);
        registerReceiver(br, filter);
        isRegFilter = true;
    }

    BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Contants.P2P.RET_SET_INIT_PASSWORD)) {
                int result = intent.getIntExtra("result", -1);
                //设置初始密码成功或者返回密码已经存在，都认为设置密码成功
                // （然后去设置访客密码，如果访客密码设置成功，就认为管理密码也设置成功了）
                if (result == Contants.P2P_SET.INIT_PASSWORD_SET.SETTING_SUCCESS || result == Contants.P2P_SET.INIT_PASSWORD_SET.ALREADY_EXIST_PASSWORD) {
                    isInitPassword = false;
                    if (!TextUtils.isEmpty(visitorPwd) && Util.isNumeric(visitorPwd)) {
                        setVisitorPwd();
                    }
                }
            } else if (intent.getAction().equals(Contants.P2P.RET_SET_VISITOR_DEVICE_PASSWORD)) {
                int result = intent.getIntExtra("result", -1);
                if (result == Contants.P2P_SET.DEVICE_VISITOR_PASSWORD_SET.SETTING_SUCCESS) {
                    if (isStopSetVisitorPwd) {
                        //防止多次返回，造成调用多次绑定接口
                        return;
                    }
                    isStopSetVisitorPwd = true;
                    setUIStep(2);
                    deviceBindMaster();
                }
            } else if (intent.getAction().equals(Contants.P2P.ACK_RET_SET_VISITOR_DEVICE_PASSWORD)) {
                int result = intent.getIntExtra("state", -1);
                int msgId = intent.getIntExtra("msgId", 0);
                //密码错误时，也重试，有可能是管理里面还没有设置成功导致的
                if (result == Contants.P2P_SET.ACK_RESULT.ACK_NET_ERROR || result == Contants.P2P_SET.ACK_RESULT.ACK_DEVICE_OFFLINE ||
                        result == Contants.P2P_SET.ACK_RESULT.ACK_PWD_ERROR) {
                    if (isStopSetVisitorPwd) {
                        return;
                    }
                    //设置访客密码ACK返回网络异常，设置访客密码ID和ip轮流发（msgId每发送一次命令，会加1，所以通过这个来确定
                    // 是用ID还是ip）之所以用IP和id轮流发，是为了增加成功率，如果手机和设备连接的是同一个网络，用ip会快些，
                    //如果不再同一个网络，用ip走不通，只能用ID
                    if (!TextUtils.isEmpty(visitorPwd) && Util.isNumeric(visitorPwd)) {
                        if (msgId % 2 == 0) {
                            P2PHandler.getInstance().setDeviceVisitorPassword(contact.getRealContactID(), contact.contactPassword, visitorPwd, 0);
                        } else {
                            P2PHandler.getInstance().setDeviceVisitorPassword(contact.getRealContactID(), contact.contactPassword, visitorPwd, contact.getDeviceIp());
                        }
                    }
                }
            } else if (intent.getAction().equals(Contants.P2P.ACK_RET_SET_DEVICE_PASSWORD)) {
                int result = intent.getIntExtra("result", -1);
                int msgId = intent.getIntExtra("msgId", 0);
                if (isStopModifyPwd) {
                    return;
                }
                if (result == Contants.P2P_SET.ACK_RESULT.ACK_PWD_ERROR || result == Contants.P2P_SET.ACK_RESULT.ACK_INSUFFICIENT_PERMISSIONS) {
                    //如果密码错误,尝试下一组密码
                    tryNextPwd();

                } else if (result == Contants.P2P_SET.ACK_RESULT.ACK_NET_ERROR || result == Contants.P2P_SET.ACK_RESULT.ACK_DEVICE_OFFLINE) {
                    if (!TextUtils.isEmpty(initPwd) && !"0".equals(initPwd)) {
                        if (msgId % 2 == 0) {
                            P2PHandler.getInstance().setDevicePassword(contact.getRealContactID(), initPwd, contact.contactPassword, contact.userPassword, contact.userPassword, contact.getDeviceIp());
                        } else {
                            P2PHandler.getInstance().setDevicePassword(contact.getRealContactID(), initPwd, contact.contactPassword, contact.userPassword, contact.userPassword, contact.getDeviceIp());
                        }

                    } else {
                        if (countModifyPwd < commonInitPwds.length) {
                            if (msgId % 2 == 0) {
                                P2PHandler.getInstance().setDevicePassword(contact.getRealContactID(), commonInitPwds[countModifyPwd], contact.contactPassword, contact.userPassword, contact.userPassword, contact.getDeviceIp());
                            } else {
                                P2PHandler.getInstance().setDevicePassword(contact.getRealContactID(), commonInitPwds[countModifyPwd], contact.contactPassword, contact.userPassword, contact.userPassword, contact.getDeviceIp());
                            }
                        }
                    }
                } else if (result == Contants.P2P_SET.ACK_RESULT.ACK_SUCCESS) {
                    //因为有时候设备收到命令，改成功密码后，返回app的消息app收不到，所以ACK返回成功，就去设置访客密码
                    isStopModifyPwd = true;
                    if (!TextUtils.isEmpty(visitorPwd) && Util.isNumeric(visitorPwd)) {
                        setVisitorPwd();
                    }
                }
            } else if (intent.getAction().equals(Contants.P2P.RET_SET_DEVICE_PASSWORD)) {
                int result = intent.getIntExtra("result", -1);
//                if (result == Contants.P2P_SET.DEVICE_PASSWORD_SET.SETTING_SUCCESS) {
//                    isStopModifyPwd = true;
//                    if (!TextUtils.isEmpty(visitorPwd) && Utils.isNumeric(visitorPwd)) {
//                        modifyPwdHandler.removeCallbacks(modifyPwdRunnable);
//                        setVisitorPwd();
//                    }
//                }
            }
        }
    };

    /**
     * 随机生成管理密码
     */
    private void randomCreatePwd() {
        //随机产生管理密码
        String[] pwds = MyUtils.createRandomPassword(0);
        contact.userPassword = pwds[0];
        contact.contactPassword = pwds[1];
    }

    /**
     * 随机访客密码(如果已经有访客密码，不再重新生成)
     */
    private void randomCreateVisitorPwd(String GuestKey) {
        if (TextUtils.isEmpty(GuestKey)) {
            //随机生成访客密码
            String[] vPwds = MyUtils.createRandomPassword(1);
            visitorUserPwd = vPwds[0];
            visitorPwd = vPwds[1];
            return;
        }
        String guestPwd = P2PHandler.getInstance().HTTPDecrypt(userId, GuestKey, 128);
        if (TextUtils.isEmpty(guestPwd) || guestPwd.equals("0")) {
            //解密出错，随机生成访客密码
            String[] vPwds = MyUtils.createRandomPassword(1);
            visitorUserPwd = vPwds[0];
            visitorPwd = vPwds[1];
        } else {
            visitorUserPwd = guestPwd;
            visitorPwd = P2PHandler.getInstance().EntryPassword(visitorUserPwd);
        }
    }

    //是否第一次设置初始密码
    boolean isFirstSetInitPwd = true;

    //设置初始密码
    private void setInitPassword() {
        if (TextUtils.isEmpty(contact.contactPassword) || !Util.isNumeric(contact.contactPassword)) {
            return;
        }
//        isFirstAckSuccess = true;
//        if (TextUtils.isEmpty(ipFrag) || !Utils.isNumeric(ipFrag)) {
//            P2PHandler.getInstance().setInitPassword(contact.getRealContactID(), contact.contactPassword, contact.userPassword, contact.userPassword);
//        } else {
//            P2PHandler.getInstance().setInitPassword(ipFrag, contact.contactPassword, contact.userPassword, contact.userPassword);
//        }
        isInitPassword = true;
        repeatInitPwd();
    }

    /**
     * 是否第一次修改密码
     */
    boolean isFirstModifyPwd = true;
    /**
     * 是否停止修改密码
     */
    boolean isStopModifyPwd = false;
    /**
     * 设备常用三组初始密码
     */
    String[] commonInitPwds = new String[]{"123", "66666666", "123456"};
    int countModifyPwd = 0;

    //如果有初始密码，修改密码
    private void modifyPwd() {
        isStopModifyPwd = false;
        if (!TextUtils.isEmpty(initPwd) && !"0".equals(initPwd)) {
            //发一条外网发一条内网
            P2PHandler.getInstance().setDevicePassword(contact.contactId, initPwd, contact.contactPassword, contact.userPassword, contact.userPassword, contact.getDeviceIp());
            if (P2PValue.DeviceMode.AP_MODE == contact.mode) {
                P2PHandler.getInstance().setDevicePassword(contact.contactId, initPwd, contact.contactPassword, contact.userPassword, contact.userPassword, 0);
            }
        } else {
            P2PHandler.getInstance().setDevicePassword(contact.contactId, commonInitPwds[countModifyPwd], contact.contactPassword, contact.userPassword, contact.userPassword, contact.getDeviceIp());
            if (P2PValue.DeviceMode.AP_MODE == contact.mode) {
                P2PHandler.getInstance().setDevicePassword(contact.contactId, commonInitPwds[countModifyPwd], contact.contactPassword, contact.userPassword, contact.userPassword, 0);
            }
        }
    }

    /**
     * 如果知道初始密码，设置失败时进入设置密码页面；如果不知道初始密码尝试下一组密码，三组密码尝试都不成功，进入设置密码页面
     */
    private void tryNextPwd() {
        if (!TextUtils.isEmpty(initPwd) && !"0".equals(initPwd)) {
            goToSetPwdActivity();
        } else {
            countModifyPwd = countModifyPwd + 1;
            if (countModifyPwd < commonInitPwds.length) {
                P2PHandler.getInstance().setDevicePassword(contact.getRealContactID(), commonInitPwds[countModifyPwd], contact.contactPassword, contact.userPassword, contact.userPassword, contact.getDeviceIp());
            } else {
                goToSetPwdActivity();
            }
        }
    }

    //是否第一次设置访客密码
    boolean isFirstSetVisitorPwd = true;
    //是否停止设置访客密码
    boolean isStopSetVisitorPwd = false;

    //设置访客密码
    private void setVisitorPwd() {
        isStopSetVisitorPwd = false;
        P2PHandler.getInstance().setDeviceVisitorPassword(contact.getRealContactID(), contact.getPassword(), visitorPwd, 0);
        sendVisitorTimes = 0;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (sendVisitorTimes < 4) {
                    if (0 == sendVisitorTimes % 2) {
                        P2PHandler.getInstance().setDeviceVisitorPassword(contact.getRealContactID(),
                                contact.getPassword(),
                                visitorPwd,
                                0);
                    } else {
                        P2PHandler.getInstance().setDeviceVisitorPassword(contact.getRealContactID(),
                                contact.getPassword(),
                                visitorPwd,
                                Util.ipToIntValue(contact.ipadressAddress));
                    }

                    try {
                        Thread.sleep(2000);
                    } catch (Exception e) {

                    }
                    sendVisitorTimes++;
                }
            }
        }).start();
    }

    boolean isInitPassword = false;

    //每隔5s设置一次初始密码
    public void repeatInitPwd() {

        new Thread() {
            @Override
            public void run() {
                try {
                    while (isInitPassword) {
                        P2PHandler.getInstance().setInitPassword(contact.getRealContactID(),
                                contact.contactPassword,
                                contact.userPassword,
                                contact.userPassword,
                                Util.ipToIntValue(contact.ipadressAddress));
                        Thread.sleep(2000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    isInitPassword = false;
                }
            }
        }.start();
//        }
    }


//    ConfirmOrCancelDialog backDialg;
//
//    //退出确认框
//    private void showBackDialog() {
//        backDialg = new ConfirmOrCancelDialog(context, ConfirmOrCancelDialog.SELECTOR_BLUE_TEXT, ConfirmOrCancelDialog.SELECTOR_GARY_TEXT);
//        backDialg.setTitle(getResources().getString(R.string.give_up_add_device));
//        backDialg.setTextYes(getResources().getString(R.string.exit));
//        backDialg.setTextNo(getResources().getString(R.string.continue_to_wait));
//        backDialg.setOnYesClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                finish();
//            }
//        });
//        backDialg.setOnNoClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                backDialg.dismiss();
//            }
//        });
//        backDialg.show();
//    }
//
//    //已经绑定提示框
//    ConfirmDialog bounddialog;
//
//    private void showAlreadyBoudDialog() {
//        if (bounddialog != null && bounddialog.isShowing()) {
//            return;
//        }
//        bounddialog = new ConfirmDialog(context);
//        bounddialog.setTitle(getResources().getString(R.string.has_bound));
//        bounddialog.setGravity(Gravity.TOP);
//        bounddialog.setTxButton(getResources().getString(R.string.i_get_it));
//        bounddialog.setOnComfirmClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
//        bounddialog.show();
//    }

    //是否第一次锁定设备
    boolean isFirstLockDevice = true;
    //调用锁定接口次数，每次有两次
    int lockDeviceCount = 0;

    //锁定设备绑定主人（30分钟后服务器自动解锁）
    private void lockDeviceBindMaster() {
        lockDeviceCount = lockDeviceCount + 1;
        HttpSend.getInstance().lockDeviceBindMaster(contact.contactId, new SubscriberListener<LockDeviceBindMasterResult>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onNext(LockDeviceBindMasterResult result) {
                if (context == null) {
                    return;
                }
                switch (result.getError_code()) {
                    case HttpErrorCode.ERROR_0:
                        String GuestKey = result.getGuestKey();
                        randomCreateVisitorPwd(GuestKey);
                        if (isCreatePassword) {
                            //没有密码，设置初始密码
                            setInitPassword();
                        } else {
                            modifyPwd();
                        }
                        break;
                    case HttpErrorCode.ERROR_10902012:
                        showInfo("锁定设备失败：回话ID不正确");
                        break;
                    case HttpErrorCode.ERROR_10905001:
                        showInfo("锁定设备失败：设备已经被其它用户绑定");
                        break;
                    case HttpErrorCode.ERROR_10905004:
                        showInfo("锁定设备失败：设备已经被其它用户锁定");
                        break;
                    default:
                        showInfo("锁定设备失败：" + result.getError_code());
                        break;
                }
            }

            @Override
            public void onError(String error_code, Throwable throwable) {
                showInfo("锁定设备失败!");
            }
        });
    }

    boolean isFirstBind = true;

    //设备绑定主人
    private void deviceBindMaster() {
        //主人权限暂时固定为271(默认给报警接收权限)
        contact.setPermission(271);
        contact.activeUser = userId;
        String currentTime = String.valueOf(System.currentTimeMillis() / 1000);
        contact.setModifyTime(currentTime);
        DeviceSync device = Util.castContact2Device(contact,userId);
        String guestKey = P2PHandler.getInstance().HTTPEncrypt(userId, visitorUserPwd, 128);
        MasterDeviceSync masterDeviceSync = new MasterDeviceSync(guestKey, device, contact.getCustomId(), contact.getMac());
        if (masterDeviceSync.getDeviceSync() == null) {
            return;
        }
        bindMaster(masterDeviceSync);

    }

    int bindCount = 0;

    private void bindMaster(final MasterDeviceSync masterDeviceSync) {
        bindCount = bindCount + 1;
        HttpSend.getInstance().deviceBindMaster(masterDeviceSync, 1, new SubscriberListener<DeviceBindMasterResult>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onNext(DeviceBindMasterResult result) {
                if (isExit) {
                    return;
                }
                if (context == null) {
                    return;
                }

                switch (result.getError_code()) {
                    case HttpErrorCode.ERROR_0:
//                        DeviceSyncSPUtils.build().with(context).saveLastUpgradeFlag(result.getLastUpgradeFlag());
                        setUIStep(3);
                        showInfo("添加设备成功！");
                        finish();
                        break;
                    case HttpErrorCode.ERROR_10902012:
                        showInfo("绑定设备失败：回话ID不正确");
                        break;
                    case HttpErrorCode.ERROR_10905001:
                        showInfo("绑定设备失败：设备已经被其它用户绑定");
                        break;
                    case HttpErrorCode.ERROR_10905004:
                        showInfo("绑定设备失败：设备已经被其它用户锁定");
                        break;
                    default:
                        showInfo("绑定设备失败：" + result.getError_code());
                        finish();
                        break;
                }
            }

            @Override
            public void onError(String error_code, Throwable throwable) {
                showInfo("绑定设备失败!");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isInitPassword = false;
        if (isRegFilter) {
            unregisterReceiver(br);
            isRegFilter = false;
        }
    }


    private void goToSetPwdActivity() {
//        et_pwd.setVisibility(View.VISIBLE);
//        btn.setVisibility(View.VISIBLE);

        Intent set_password = new Intent(context, SetDevicePwdActivity.class);
        set_password.putExtra("contact", contact);
        set_password.putExtra("visitorPwd", visitorPwd);
        set_password.putExtra("visitorUserPwd", visitorUserPwd);
        set_password.putExtra("ipAddress", ipAddress);
        startActivity(set_password);
//        finish();
    }

    private void showInfo(String text) {
        String info = tx_receive.getText().toString().trim();
        tx_receive.setText(info + "\n" + text);
    }

    private void setUIStep(int step) {
        String info = tx_receive.getText().toString().trim();
        switch (step) {
            case 1:
                //设备已上线（设备正在锁定设备或是正在设置密码）
                tx_receive.setText(info + "\n设备已上线");
                break;
            case 2:
                //正在绑定设备
                tx_receive.setText(info + "\n正在绑定设备...");
                break;
            case 3:
                //绑定成功
                tx_receive.setText(info + "\n绑定成功");
                break;
            default:
                break;
        }
    }

}

