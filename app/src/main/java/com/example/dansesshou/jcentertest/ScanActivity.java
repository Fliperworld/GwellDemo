package com.example.dansesshou.jcentertest;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.gwelldemo.R;
import com.jwsd.libzxing.OnQRCodeListener;
import com.jwsd.libzxing.QRCodeManager;
import com.libhttp.entity.LockDeviceBindMasterResult;
import com.libhttp.subscribers.SubscriberListener;
import com.p2p.core.P2PHandler;
import com.p2p.core.P2PSpecial.HttpErrorCode;
import com.p2p.core.P2PSpecial.HttpSend;
import com.p2p.core.utils.SharedPrefreUtils;

import Utils.Contants;
import Utils.Util;
import entity.QRcodeAddDevice;
import entity.QRcodeScanNetworkMode;
import entity.ShareUrlEntity;

public class ScanActivity extends AppCompatActivity {
    private static final String TAG = "ScanActivity";
    private QRcodeScanNetworkMode qRcodeScanNetworkMode;
    private int connectType;
    private String visitorUserPwd;
    private String visitorPwd;
    private String userId;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_add);
        userId = SharedPrefreUtils.getInstance().getStringData(this, Contants.USERID);
        startScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        QRCodeManager.getInstance().with(this).onActivityResult(requestCode, resultCode, data);
    }

    private void startScan() {
        QRCodeManager.getInstance()
                .with(this)
                .setReqeustType(1)
                .setSupportDecodeType(QRCodeManager.SupportDecodeType.SUPPORT_QRCODE)
                .scanningQRCode(new OnQRCodeListener() {
                    @Override
                    public void onCompleted(String result) {
                        Log.e(TAG, "result=" + result);
                        ShareUrlEntity shareEntity = new ShareUrlEntity(result);
                        if (shareEntity.isShareUrl()) {
                            //分享 当前二维码内容表示是通过二维码进行分享设备
                        } else {
                            QRcodeAddDevice qRcodeAddDevice = new QRcodeAddDevice(result);
                            if (qRcodeAddDevice.isQRcodeAddDevice()) {
                                //此二维码内容表示通过NVR方式添加
                            } else {
                                qRcodeScanNetworkMode = new QRcodeScanNetworkMode(result);
                                if (qRcodeScanNetworkMode.isNetworkModeQRcode()) {
                                    switch (qRcodeScanNetworkMode.getNetworkMode()) {
                                        case QRcodeScanNetworkMode.NetworkMode.SMARTLINK:
                                        case QRcodeScanNetworkMode.NetworkMode.SOUND_WAVE:
                                        case QRcodeScanNetworkMode.NetworkMode.SIMPECONFIG:
                                        case QRcodeScanNetworkMode.NetworkMode.AP:
                                        case QRcodeScanNetworkMode.NetworkMode.SCAN:
                                            // 此处可加入扫描进度加载动画
                                            isBind();
                                            break;
                                        case QRcodeScanNetworkMode.NetworkMode.BLUETOOTH:
                                        default:
                                            Log.d(TAG, "当前二维码内容:" + result);
                                            finish();
                                            break;
                                    }
                                } else {
                                    Log.d(TAG, "当前二维码内容:" + result);
                                    finish();
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Toast.makeText(ScanActivity.this, "请确认当前二维码是否有效", Toast.LENGTH_LONG).show();
                        finish();
                    }

                    @Override
                    public void onCancel() {

                    }
                });
    }

    /**
     * 查询设备是否已经被绑定
     */
    private void isBind() {
        if (qRcodeScanNetworkMode == null || TextUtils.isEmpty(qRcodeScanNetworkMode.getDeviceID())) {
            return;
        }

        HttpSend.getInstance().lockDeviceBindMaster(qRcodeScanNetworkMode.getDeviceID(), new SubscriberListener<LockDeviceBindMasterResult>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onNext(LockDeviceBindMasterResult result) {
                Log.e("ChooseAddDevice", "result = " + result);
                if (Util.isTostCmd(result)) {
                    Toast.makeText(ScanActivity.this, Util.GetToastCMDString(result), Toast.LENGTH_LONG).show();
                    return;
                }
                switch (result.getError_code()) {
                    case HttpErrorCode.ERROR_0:
                        String GuestKey = result.getGuestKey();
                        randomCreateGuestPwd(GuestKey);
                        goConfigNetwork();
                        break;
                    case HttpErrorCode.ERROR_10902012:
                        Toast.makeText(ScanActivity.this, "Session失效，请重新登录", Toast.LENGTH_LONG).show();
                        break;
                    case HttpErrorCode.ERROR_10905001:
                        Toast.makeText(ScanActivity.this, "当前设备已被其他用户绑定", Toast.LENGTH_LONG).show();
                        break;
                    case HttpErrorCode.ERROR_10905004:
                        Toast.makeText(ScanActivity.this, "当前设备已被锁定", Toast.LENGTH_LONG).show();
                        break;
                    default:
                        Toast.makeText(ScanActivity.this, "操作失败", Toast.LENGTH_LONG).show();
                        break;
                }

            }

            @Override
            public void onError(String error_code, Throwable throwable) {
                Toast.makeText(ScanActivity.this, "配置失败", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * 向服务器查询到未绑定或是被自己绑定后，根据扫描结果进入不同的配网方式
     */
    private void goConfigNetwork() {
        if (qRcodeScanNetworkMode == null || TextUtils.isEmpty(qRcodeScanNetworkMode.getDeviceID())) {
            return;
        }
        switch (qRcodeScanNetworkMode.getNetworkMode()) {
            case QRcodeScanNetworkMode.NetworkMode.SMARTLINK:
            case QRcodeScanNetworkMode.NetworkMode.SOUND_WAVE:
                connectType = Contants.ConnectWifiType.SMART_LINK;
                Log.d(TAG, "smartlink connect");
                finish();
                break;
            case QRcodeScanNetworkMode.NetworkMode.SCAN:
                Log.d(TAG, "qrcode scan add");
                Intent scan = new Intent(ScanActivity.this, SmartLinkConfigWifiActivity.class);
                scan.putExtra("connectType", Contants.ConnectWifiType.SCAN);
                scan.putExtra("visitorUserPwd", visitorUserPwd);
                scan.putExtra("deviceId", qRcodeScanNetworkMode.getDeviceID());
                startActivity(scan);
                finish();
                break;
            case QRcodeScanNetworkMode.NetworkMode.SIMPECONFIG:
                connectType = Contants.ConnectWifiType.SIMPLECONFIG;
                Log.d(TAG, "simpleconfig connect");
                finish();
                break;
            case QRcodeScanNetworkMode.NetworkMode.AP:
                Log.d(TAG, "ap add");
                goAp();
                break;
            case QRcodeScanNetworkMode.NetworkMode.BLUETOOTH:
            default:
                break;
        }
    }

    // AP 配网添加方式
    private void goAp() {

    }


    /**
     * 随机创建访客密码(如果已经有访客密码，不再重新生成)
     */
    private void randomCreateGuestPwd(String GuestKey) {
        if (TextUtils.isEmpty(GuestKey)) {
            //随机生成访客密码
            String[] vPwds = Util.createRandomPassword(1);
            visitorUserPwd = vPwds[0];
            visitorPwd = vPwds[1];
            return;
        }
        String guestPwd = P2PHandler.getInstance().HTTPDecrypt(userId, GuestKey, 128);
        if (TextUtils.isEmpty(guestPwd) || guestPwd.equals("0")) {
            //解密出错，随机生成访客密码
            String[] vPwds = Util.createRandomPassword(1);
            visitorUserPwd = vPwds[0];
            visitorPwd = vPwds[1];
        } else {
            visitorUserPwd = guestPwd;
            visitorPwd = P2PHandler.getInstance().EntryPassword(visitorUserPwd);
        }
    }
}
