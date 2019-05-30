package com.example.dansesshou.jcentertest;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gwelldemo.R;
import com.jwsd.libzxing.QRCodeManager;
import com.libhttp.entity.GetInviteCodeResult;
import com.libhttp.entity.GetVisitorInformationResult;
import com.libhttp.subscribers.SubscriberListener;
import com.p2p.core.P2PSpecial.HttpErrorCode;
import com.p2p.core.P2PSpecial.HttpSend;
import com.p2p.core.permission.VisitorPermission;

import Utils.ToastUtils;
import Utils.Util;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import sdk.MyApp;

public class ShareDeviceActivity extends AppCompatActivity {

    @BindView(R.id.et_id)
    EditText etId;
    @BindView(R.id.et_phone_email)
    EditText etPhoneEmail;
    @BindView(R.id.btn)
    Button btn;
    @BindView(R.id.txt)
    TextView txt;
    @BindView(R.id.iv_qrcode)
    ImageView ivQrcode;
    @BindView(R.id.btn_phone_email)
    Button btnPhoneEmail;
    @BindView(R.id.btn_link)
    Button btnLink;
    private Context context;

    private static final int SHARE_BY_FACE_TO_FACE = 1;
    private static final int SHARE_BY_LINK = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_device);
        ButterKnife.bind(this);
        context = this;
        userId = getIntent().getStringExtra(LoginActivity.USERID);

    }

    String contactId,userId;

    @OnClick(R.id.btn)
    public void onViewClicked() {
        contactId = etId.getText().toString().trim();//设备号
        if (TextUtils.isEmpty(contactId) ) {
            Toast.makeText(context, "请输入设备ID", Toast.LENGTH_SHORT).show();
            return;
        }
        getInviteCode(SHARE_BY_FACE_TO_FACE,analyzeData(),"1");
    }
    @OnClick(R.id.btn_link)
    public void onShareByLink() {
        contactId = etId.getText().toString().trim();//设备号
        if (TextUtils.isEmpty(contactId) ) {
            Toast.makeText(context, "请输入设备ID", Toast.LENGTH_SHORT).show();
            return;
        }
        getInviteCode(SHARE_BY_LINK,analyzeData(),"1");
    }

    @OnClick(R.id.btn_phone_email)
    public void onShareByPhoneOrEmail(){
        contactId = etId.getText().toString().trim();//设备号
        String guestAccount = etPhoneEmail.getText().toString().trim();
        if (TextUtils.isEmpty(guestAccount)) {
            Toast.makeText(context, "请输入对方手机号或邮箱", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Util.isNumeric(guestAccount)) {
            //手机号
            if (guestAccount.charAt(0) == '0' || !Util.isMobileNO(guestAccount)) {
                Toast.makeText(context, "手机号或邮箱格式错误", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            if (!Util.isEmail(guestAccount) && !Util.isMobileNOAddCountryCode(guestAccount)) {
                Toast.makeText(context, "手机号或邮箱格式错误", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        getVisitorInformation(contactId, guestAccount);
    }

    String remarkName;

    /**
     * 获取访客信息
     *
     * @param deviceID     设备ID
     * @param guestAccount 访客手机号或邮箱
     */
    private void getVisitorInformation(String deviceID, String guestAccount) {
        HttpSend.getInstance().getVisitorInformation(deviceID, guestAccount, new SubscriberListener<GetVisitorInformationResult>() {
            @Override
            public void onStart() {

                showInfo("开始获取访客信息...");
            }

            @Override
            public void onNext(GetVisitorInformationResult result) {

                switch (result.getError_code()) {
                    case HttpErrorCode.ERROR_0:
                        showInfo("获取访客信息成功:"+result);
//                        visitorInformationResult = result;
                        if (TextUtils.isEmpty(result.getRemarkName()) || "null".equals(result.getRemarkName())) {
                            remarkName =result.getUserID();
                        } else {
                            if (Util.isEmail(result.getRemarkName())) {
                                remarkName = Util.getEmaiStringlimit24(result.getRemarkName());
                            } else {
                                if (result.getRemarkName().length() > 24) {
                                    remarkName = result.getRemarkName().substring(0, 24);
                                } else {
                                    remarkName = result.getRemarkName();
                                }
                            }
                        }
                        //给访客的留言，可使用户自定义
                        String massage = "邀请你观看我的摄像头";
                        if (result != null && !TextUtils.isEmpty(result.getUserID())) {
                            shareByPhoneOrEmail(result.getUserID(), remarkName, massage,analyzeData());
                        }else {
                            showInfo("获取的访客信息不正确");
                        }
//                        ll_search.setVisibility(View.GONE);
//                        ll_share.setVisibility(View.VISIBLE);
//                        tx_search.setVisibility(View.VISIBLE);
                        break;
                    case HttpErrorCode.ERROR_10902012:
                        showInfo("获取访客信息失败:会话ID不正确");
                        break;
                    case HttpErrorCode.ERROR_10905010:
                        showInfo("获取访客信息失败:已经是访客了");
                        break;
                    case HttpErrorCode.ERROR_10902011:
                        showInfo("获取访客信息失败:用户不存在");
                        break;
                    case HttpErrorCode.ERROR_10905009:
                        showInfo("获取访客信息失败:已经是设备主人");
                        break;
                    default:
                        showInfo("获取访客信息失败:"+result.getError_code());
                        break;
                }

            }

            @Override
            public void onError(String error_code, Throwable throwable) {
                showInfo("获取访客信息失败:"+error_code);
            }
        });
    }

    //解析数据
    private String analyzeData() {
//        if (!(contact.isStartPermissionManage() && contact.getAddType() != Constants.AddType.OLD_DEVICE)) {
//            return "61";//默认提供：监控、摇头、回放、对讲
//        }
//        //授予默认权限5
//        VisitorPermission visitorPermission = new VisitorPermission(5);
//        for (int i = 0; i < list.size(); i++) {
//            PermissionItem item = list.get(i);
//            visitorPermission.setItemPermission(item.getPeimissionIndex(), item.getSwitchState());
//        }
//        if (0 == visitorPermission.getPermission()) {
//            return "61";//默认提供：监控、摇头、回放、对讲
//        }
//        return String.valueOf(visitorPermission.getPermission());

        //默认提供：监控、摇头、回放、对讲
        //实际应取值获取设置的访客权限
        return "61";
    }

    /**
     *
     * @param permission
     * @param mode 是否允许服务器修改默认权限,"1":不允许；其他：允许
     */
    //获取邀请码
    private void getInviteCode(final int type,String permission, String mode) {
        if (TextUtils.isEmpty(permission)) {
            permission = "61";//默认提供：监控、摇头、回放、对讲
        }
        final String initPermission = permission;
        //该web接口要满足条件：
        // 1.用可分享方式添加设备 2.主人调用此接口
        //才会回正确结果
        HttpSend.getInstance().getInviteCodeByMode(contactId, initPermission, mode,new SubscriberListener<GetInviteCodeResult>() {
            @Override
            public void onStart() {
                showInfo("开始获取邀请码...");
            }

            @Override
            public void onNext(GetInviteCodeResult result) {
                switch (result.getError_code()) {
                    case HttpErrorCode.ERROR_0:
                        showInfo("获取邀请码成功："+result);
                        if (type == SHARE_BY_FACE_TO_FACE){
                            int width = Util.dip2px(context, (float) 130);
                            Bitmap bitmap = QRCodeManager.getInstance().createQRCode(result.getShareLink(), width, width);
                            ivQrcode.setImageBitmap(bitmap);
                        }else if (type == SHARE_BY_LINK){
                            showInfo("邀请码链接："+result.getShareLink());
                            ClipboardManager clipboardManager = (ClipboardManager) MyApp.app.getSystemService(Context.CLIPBOARD_SERVICE);
                            clipboardManager.setPrimaryClip(ClipData.newPlainText(result.getShareLink(),result.getShareLink()));
                            showInfo("已复制到粘贴板！可使用链接分享");
                        }

                        break;
                    case HttpErrorCode.ERROR_10902012:
                        showInfo("会话ID不正确");
                        break;
                    default:
                        showInfo("获取邀请码失败："+result.getError_code());
                        break;
                }

            }

            @Override
            public void onError(String error_code, Throwable throwable) {
                showInfo("获取邀请码失败："+error_code);
            }
        });
    }

    private void shareByPhoneOrEmail(final String GuestID, final String GuestRemark, String AppendMsg, String permission) {
        if (TextUtils.isEmpty(permission)) {
            permission = "61";//默认提供：监控、摇头、回放、对讲
        }
        HttpSend.getInstance().shareByPhoneOrEmail(contactId, permission, GuestID, GuestRemark, AppendMsg,"1", new SubscriberListener<GetInviteCodeResult>() {
            @Override
            public void onStart() {
                showInfo("开始根据手机号码或者邮箱分享设备...");
            }

            @Override
            public void onNext(GetInviteCodeResult result) {
                switch (result.getError_code()) {
                    case HttpErrorCode.ERROR_0:
                        showInfo("根据手机号码或者邮箱分享设备成功");
                        showInfo("邀请信息已发送！");
                        break;
                    case HttpErrorCode.ERROR_10902012:
                        showInfo("根据手机号码或者邮箱分享设备失败：会话ID不正确");
                        break;
                    default:
                        showInfo("根据手机号码或者邮箱分享设备失败："+result.getError_code());
                        break;
                }
            }

            @Override
            public void onError(String error_code, Throwable throwable) {
                showInfo("根据手机号码或者邮箱分享设备失败："+error_code);
            }
        });
    }

    private void showInfo(String text){
        String info =txt.getText().toString().trim();
        txt.setText(info+"\n"+text);
    }
}
