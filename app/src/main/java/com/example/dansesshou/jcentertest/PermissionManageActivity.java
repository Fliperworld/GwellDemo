package com.example.dansesshou.jcentertest;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gwelldemo.R;
import com.libhttp.entity.GetGuestListResult;
import com.libhttp.entity.HttpResult;
import com.libhttp.entity.ModifyPermissionResult;
import com.libhttp.subscribers.SubscriberListener;
import com.p2p.core.P2PSpecial.HttpErrorCode;
import com.p2p.core.P2PSpecial.HttpSend;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PermissionManageActivity extends AppCompatActivity {


    @BindView(R.id.et_id)
    EditText etId;
    @BindView(R.id.et_visitor_id)
    EditText etVisitorId;
    @BindView(R.id.btn_get_visitor_list)
    Button btnGetVisitorList;
    @BindView(R.id.txt)
    TextView txt;
    @BindView(R.id.btn_set_visitor_permission)
    Button btnSetVisitorPermission;
    @BindView(R.id.btn_delete_visitor)
    Button btnDeleteVisitor;
    @BindView(R.id.btn_remark_name)
    Button btnRemarkName;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_manage);
        ButterKnife.bind(this);
        context = this;
    }

    @OnClick(R.id.btn_get_visitor_list)
    public void getVisitorList() {
        String contactId = etId.getText().toString().trim();
        if (TextUtils.isEmpty(contactId)){
            Toast.makeText(context, "请输入设备id", Toast.LENGTH_SHORT).show();
            return;
        }
        HttpSend.getInstance().getGuestList(contactId, new SubscriberListener<GetGuestListResult>() {
            @Override
            public void onStart() {
                showInfo("开始获取访客列表...");
            }

            @Override
            public void onNext(GetGuestListResult result) {
                Log.e("zxy", "getVisitorsList onNext: " + result.getError_code());
                switch (result.getError_code()) {
                    case HttpErrorCode.ERROR_0:
                        showInfo("获取访客列表成功："+result);
                        break;
                    case HttpErrorCode.ERROR_10902012:
                        showInfo("获取访客列表失败：会话ID不正确");
                        break;
                    default:
                        showInfo("获取访客列表失败："+result.getError_code());
                        break;
                }
            }

            @Override
            public void onError(String error_code, Throwable throwable) {
                showInfo("获取访客列表失败："+error_code);
            }
        });
    }

    @OnClick(R.id.btn_set_visitor_permission)
    public void setVisitorPermission() {
        String contactId = etId.getText().toString().trim();
        if (TextUtils.isEmpty(contactId)){
            Toast.makeText(context, "请输入设备id", Toast.LENGTH_SHORT).show();
            return;
        }
        String visitorId = etVisitorId.getText().toString().trim();
        if (TextUtils.isEmpty(visitorId)){
            Toast.makeText(context, "请输入访客id", Toast.LENGTH_SHORT).show();
            return;
        }
        /**
         * 关于权限的定义，Permission是int8类型，总共有64个bit，从低位到高位分配如下：
         * Bit0:表示是否启用权限管理功能；
         * Bit1:表示是否是设备的主人（主人不受其他权限位的影响；不是主人才需要根据权限位的定义处理）；
         * Bit2：表示是否有基本权限（看视频、截屏、录制本地视频、查看当前流量，调整画质[分辨率]）；
         * Bit3：表示是否有摇头权限
         * Bit4：表示是否有语音（含对讲）权限；
         * Bit5：表示是否有回放（包含云回放）权限；
         * Bit6：表示是否有布撤防权限；
         * Bit7：表示是否有开锁（支持锁的设备才显示开锁权限设置）权限；
         * Bit8：表示是否有接收设备消息（报警、门铃、呼叫等）推送权限；
         * Bit9  表示是否开启接收设备消息（当Bit8为1，此位才有效）；
         * Bit10 表示设备离线后是否开启接收离线通知（0：关闭离线通知，1：开启离线通知）；
         */
        //权限
        final String permision = "189";
        HttpSend.getInstance().modifyPermission(contactId, visitorId, permision, new SubscriberListener<ModifyPermissionResult>() {
            @Override
            public void onStart() {
                Log.e("zxy", "onStart: modifyPermission");
                showInfo("开始修改权限...");
            }

            @Override
            public void onNext(ModifyPermissionResult result) {

                Log.e("zxy", "onNext: modifyPermission:"+result.getError_code());
                switch (result.getError_code()) {
                    case HttpErrorCode.ERROR_0:
                        showInfo("权限修改成功");
                        break;
                    case HttpErrorCode.ERROR_10902012:
                        showInfo("权限修改失败：会话ID不正确");
                        break;
                    default:
                        showInfo("权限修改失败："+result.getError_code());
                        break;
                }
            }

            @Override
            public void onError(String error_code, Throwable throwable) {
                Log.e("zxy", "onError: modifyPermission:"+error_code);
                showInfo("权限修改失败："+error_code);
            }
        });
    }

    @OnClick(R.id.btn_delete_visitor)
    public void deleteVisitor() {
        String contactId = etId.getText().toString().trim();
        if (TextUtils.isEmpty(contactId)){
            Toast.makeText(context, "请输入设备id", Toast.LENGTH_SHORT).show();
            return;
        }
        String visitorId = etVisitorId.getText().toString().trim();
        if (TextUtils.isEmpty(visitorId)){
            Toast.makeText(context, "请输入访客id", Toast.LENGTH_SHORT).show();
            return;
        }

        HttpSend.getInstance().deleteGuestInfo(contactId, visitorId, new SubscriberListener<HttpResult>() {
            @Override
            public void onStart() {
                showInfo("开始删除访客...");
            }

            @Override
            public void onNext(HttpResult result) {
                switch (result.getError_code()) {
                    case HttpErrorCode.ERROR_0:
                        showInfo("删除访客成功");
                        break;
                    case HttpErrorCode.ERROR_10902012:
                        showInfo("删除访客失败：会话ID不正确");
                        break;
                    default:
                        showInfo("删除访客失败："+result.getError_code());
                        break;
                }

            }

            @Override
            public void onError(String error_code, Throwable throwable) {
                showInfo("删除访客失败："+error_code);
            }
        });
    }

    @OnClick(R.id.btn_remark_name)
    public void remarkName() {
        String contactId = etId.getText().toString().trim();
        if (TextUtils.isEmpty(contactId)){
            Toast.makeText(context, "请输入设备id", Toast.LENGTH_SHORT).show();
            return;
        }
        String visitorId = etVisitorId.getText().toString().trim();
        if (TextUtils.isEmpty(visitorId)){
            Toast.makeText(context, "请输入访客id", Toast.LENGTH_SHORT).show();
            return;
        }

        //此处 remarkName 暂时用"test"  具体情况客户自定义
        HttpSend.getInstance().modifyVisitorRemarkName(contactId, visitorId,"test", new SubscriberListener<HttpResult>() {
            @Override
            public void onStart() {
                showInfo("开始修改访客备注...");
            }

            @Override
            public void onNext(HttpResult result) {
                switch (result.getError_code()) {
                    case HttpErrorCode.ERROR_0:
                        showInfo("修改访客备注成功");
                        break;
                    case HttpErrorCode.ERROR_10902012:
                        showInfo("修改访客备注失败：会话ID不正确");
                        break;
                    default:
                        showInfo("修改访客备注失败："+result.getError_code());
                        break;
                }

            }

            @Override
            public void onError(String error_code, Throwable throwable) {
                showInfo("修改访客备注失败："+error_code);
            }
        });
    }

    private void showInfo(String text){
        String info =txt.getText().toString().trim();
        txt.setText(info+"\n"+text);
    }


}
