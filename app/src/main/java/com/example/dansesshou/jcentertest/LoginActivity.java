package com.example.dansesshou.jcentertest;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gwelldemo.R;
import com.libhttp.entity.LoginResult;
import com.libhttp.subscribers.SubscriberListener;
import com.p2p.core.MediaPlayer;
import com.p2p.core.P2PHandler;
import com.p2p.core.P2PSpecial.HttpErrorCode;
import com.p2p.core.P2PSpecial.HttpSend;
import com.p2p.core.utils.SharedPrefreUtils;

import Utils.ToastUtils;
import butterknife.ButterKnife;
import butterknife.OnClick;
import sdk.MyApp;
import sdk.P2PListener;
import sdk.SettingListener;


/**
 * A login screen that offers login via email/password.
 * 此页面是Android studio 自动生成的具体登陆页需用户自己实现
 */
public class LoginActivity extends AppCompatActivity {
    public final static String USERID="USERID";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private String userId;
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        mContext=this;
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        mPasswordView = (EditText) findViewById(R.id.password);
        if (mPasswordView == null) {
            Log.e("dxsTest", "mPasswordView==null");
        }
        mEmailView.setText(SharedPrefreUtils.getInstance().getStringData(mContext,USERNAME));
        mPasswordView.setText(SharedPrefreUtils.getInstance().getStringData(mContext,PASSWORD));

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            //*************************技威代码插入**********************************
            //除了LoginResult、AccountInfoResult、AccountInfoResult、GetStartInfoResult、HttpData、MallUrlResult、ModifyLoginPasswordResult、SystemMessageResult
            //其他都使用HttpResult
            SubscriberListener<LoginResult> subscriberListener = new SubscriberListener<LoginResult>() {

                @Override
                public void onStart() {
                    showProgress(true);
                }

                @Override
                public void onNext(LoginResult loginResult) {
                    showProgress(false);
                    //error code 全部改为了新版,如果没有老版对应 的反馈码则可忽略此错误
                    //如果不可以忽略,则反馈给技术支持即可
                    switch (loginResult.getError_code()) {
                        case HttpErrorCode.ERROR_0:
                            //成功的逻辑不需要改成下面这样,以下仅演示过程
                            //原有的这部分代码可以不修改
                            showProgress(false);
                            //code1与code2是p2p连接的鉴权码,只有在帐号异地登录或者服务器强制刷新(一般不会干这件事)时才会改变
                            //所以可以将code1与code2保存起来,只需在下次登录时刷新即可
                            saveAuthor(loginResult);
                            P2PHandler.getInstance().p2pInit(mContext, new P2PListener(), new SettingListener());
                            setDeviceP2pVersion();
                            SharedPrefreUtils.getInstance().putStringData(mContext, USERNAME, mEmailView.getText().toString());
                            SharedPrefreUtils.getInstance().putStringData(mContext, PASSWORD, mPasswordView.getText().toString());
                            Intent callIntent = new Intent(MyApp.app, MainActivity.class);
                            callIntent.putExtra(LoginActivity.USERID, userId);
                            startActivity(callIntent);
                            finish();
                            break;
                        case HttpErrorCode.ERROR_10902011:
                            showProgress(false);
                            ToastUtils.ShowError(MyApp.app,"用户不存在",Toast.LENGTH_LONG,true);
                            break;
                        case HttpErrorCode.ERROR_10902003:
                            showProgress(false);
                            mPasswordView.setError(getString(R.string.error_incorrect_password));
                            mPasswordView.requestFocus();
                            break;
                        default:
                            //其它错误码需要用户自己实现
                            showProgress(false);
                            String msg = String.format("登录失败测试版(%s)", loginResult.getError_code());
                            ToastUtils.ShowError(MyApp.app,msg,Toast.LENGTH_LONG,true);
                            break;
                    }
                }

                @Override
                public void onError(String error_code, Throwable throwable) {
                    showProgress(false);
                    Toast.makeText(MyApp.app, "onError:" + error_code, Toast.LENGTH_LONG).show();
                }
            };
            //支持邮箱,手机号码(必须带国码 eg:86-18922222222),用户ID
            HttpSend.getInstance().login(email, password, subscriberListener);
            //*************************技威代码插入**********************************
        }
    }

    private void setDeviceP2pVersion() {
        /**
         * 监控之前需要设设备p2p版本给p2p库，需要在ISetting.vRetGetIndexFriendStatus 获取到用户所有设备信息后，
         * 把所有设备p2p版本信息设置给MediaPlayer.setP2PLibVersion
         * 建议将该信息存入数据库
         *      * 设置p2p库的版本号
         *      * @param devTable 设备列表
         *      * @param versionTable  p2p库的版本号列表  @see ISetting.vRetGetIndexFriendStatus  @param p2pLibVersion  当前设备P2P库的版本
         *      ISetting.vRetGetIndexFriendStatus 注释如下
         *      * @param count 设备数量
         *      * @return
         *
         *    public static native boolean setP2PLibVersion ( int[] devTable, short[] versionTable,int count);
         *
         *
         *      * Index服务器返回设备信息（区别于P2P服务器返回数据，存在兼容标记）
         *      *
         *      * @param count          设备信息数量
         *      * @param contactIds     设备ID
         *      * @param IdProtery      设备属性 &0x1==1（最低位为1）则支持Index服务器
         *      * @param status         设备在线状态 0:离线 1:在线
         *      * @param DevTypes       设备类型
         *      * @param SubType        设备子类型（需支持Index服务器）
         *      * @param DefenceState   设备布撤防状态（需支持Index服务器）
         *      * @param bRequestResult Index请求结果标记  非0时正常  为0时需要重新请求P2P服务器
         *      * @param defenceFlag    布撤防状态标记(主要用于判断index服务器与设备返回哪个值较新,如果index返回的flag比设备返回的flag较小，则丢弃index的布撤防返回状态)
         *      * @param p2pLibVersion  当前设备P2P库的版本
         *
         *     void vRetGetIndexFriendStatus ( int count, String[] contactIds,int[] IdProtery,
         *    int[] status, int[] DevTypes, int[] SubType, int[] DefenceState, byte bRequestResult,
         *     long[] defenceFlag,int[][] configs, int[][] infos, int[] startAuthManage, short[] p2pLibVersion);
         *
         */
        MediaPlayer.setP2PLibVersion(new int[]{9082821}, new short[]{1284}, 1);
    }

    private void saveAuthor(LoginResult loginResult){
        int code1 = Integer.parseInt(loginResult.getP2PVerifyCode1());
        int code2 = Integer.parseInt(loginResult.getP2PVerifyCode2());
        String sessionId =loginResult.getSessionID();
        String sessionId2 =loginResult.getSessionID2();
        userId =  loginResult.getUserID();
        SharedPreferences sp=getSharedPreferences("Account",MODE_PRIVATE);
        SharedPreferences.Editor editor =sp.edit();
        editor.putInt("code1",code1);
        editor.putInt("code2",code2);
        editor.putString("sessionId",sessionId);
        editor.putString("sessionId2",sessionId2);
        editor.putString("userId",userId);
        editor.apply();
    }

    private boolean isEmailValid(String email) {
        return true;
        //return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 0;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @OnClick(R.id.email_register_button)
    public void onClick() {
        Intent intentReg = new Intent(this,RegisterActivity.class);
        startActivityForResult(intentReg,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==1&& resultCode==2){
            String email =data.getStringExtra("email");
            String password=data.getStringExtra("password");
            if (!TextUtils.isEmpty(email)&&!TextUtils.isEmpty(password)){
                mEmailView.setText(email);
                mPasswordView.setText(password);
                mEmailView.setSelection(email.length());
                mPasswordView.setSelection(password.length());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}

