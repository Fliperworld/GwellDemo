package Utils;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.text.TextUtils;

import com.blankj.utilcode.util.TimeUtils;
import com.libhttp.entity.DeviceSync;
import com.libhttp.entity.HttpResult;
import com.p2p.core.P2PHandler;
import com.p2p.core.P2PSpecial.HttpErrorCode;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import entity.Contact;

/**
 * Created by dansesshou on 17/2/17.
 */

public class Util {

    public static InetAddress getIntentAddress(Context mContext)
            throws IOException {
        WifiManager wifiManager = (WifiManager) mContext
                .getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifiManager.getDhcpInfo();
        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }

    public static String getScreenShotPath(){
        String path= Environment.getExternalStorageDirectory().getPath();
        return path;
    }

    public static String IPPasser(String netAddress){
        if(TextUtils.isEmpty(netAddress)){
            return "";
        }
        try{
            InetAddress x = java.net.InetAddress.getByName(netAddress);
            return x.getHostAddress();//得到字符串形式的ip地址
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
        return "";
    }

    public static String getLogStr(String str){
        long time=System.currentTimeMillis();
        String Time= TimeUtils.millis2String(time,"HH:mm:ss");
        return String.format("%s:%s\n",Time,str);
    }

    public static boolean isNumeric(String str) {
        if (null == str || "".equals(str)) {
            return false;
        }
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    /** 将ip地址转换成int值
     * @param inetAddress
     * @return
     */
    public static int ipToIntValue(InetAddress inetAddress) {
        int addr;
        if (null == inetAddress) {
            return 0;
        }
        byte[] addrBytes;
        addrBytes = inetAddress.getAddress();
        addr = ((addrBytes[3] & 0xff) << 24)| ((addrBytes[2] & 0xff)<< 16)
                | ((addrBytes[1] & 0xff) << 8)
                | (addrBytes[0] & 0xff);
        return addr;
    }

    public static String getBitProcessingVersion(String deviceVersion) {
        try {
            String[] parseVerson = deviceVersion.split("\\.");
            int a = Integer.parseInt(parseVerson[0]) << 24;
            int b = Integer.parseInt(parseVerson[1]) << 16;
            int c = Integer.parseInt(parseVerson[2]) << 8;
            int d = Integer.parseInt(parseVerson[3]);
            return String.valueOf((a | b | c | d));
        } catch (Exception e) {
            return "0";
        }
    }

    /**
     * 转换本地的设备为需要上传的设备对象
     *
     * @param contact
     * @return
     */
    public static DeviceSync castContact2Device(Contact contact,String userId) {
        if (contact == null || TextUtils.isEmpty(userId)) {
            return null;
        }
        String remarkname = contact.contactName;
        remarkname = remarkname.replace("|", "").replace(",", "");
        contact.contactName = remarkname;
        DeviceSync device = new DeviceSync();
        device.setModifyTime(contact.getModifyTime() + "");
        device.setDropFlag(contact.getDropFlag() + "");
        device.setDeviceID(contact.contactId);
        String contactName = contact.contactName;
        device.setRemarkName(contactName);
        String userPassword = contact.userPassword;
        if (TextUtils.isEmpty(userPassword)) {//用户密码为空时使用数字密码
            userPassword = contact.contactPassword;
        }
        if (TextUtils.isEmpty(userPassword)) {
            return null;
        }
        try {
            device.setSecretKey(P2PHandler.getInstance().HTTPEncrypt(userId, userPassword, 128));
            device.setDeviceInfoVersion(TextUtils.isEmpty(contact.cur_version) ? "1" : getBitProcessingVersion(contact.cur_version));
//        KLog.e("这个对象转换(本地)：" + contact + "\n变成这个对象(服务器)" + device);
            device.setPermission(String.valueOf(contact.getPermission()));
        } catch (NullPointerException e) {
            return null;
        }
        return device;
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * 简版国际手机号判断
     *
     * @param mobiles
     * @return
     */
    public static boolean isMobileNO(String mobiles) {
        if (isNumeric(mobiles)) {
            if (mobiles.length() > 5 && mobiles.length() < 16) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEmail(String str) {
//		Pattern pattern = Pattern.compile(emailRegex, Pattern.CASE_INSENSITIVE);
//		Matcher matcher = pattern.matcher(str);
//		return matcher.matches();
        return (str.contains("@") && str.contains("."));
    }

    /**
     * 手机号加国码的格式判断（+国码-手机号）
     *
     * @return
     */
    public static boolean isMobileNOAddCountryCode(String mInputName) {
        String regex = "^\\+\\d{1,5}\\-{1}\\d{6,15}$";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(mInputName);
        return matcher.matches();
    }

    /**
     * 获取邮箱总长度限制为24位，超过24位在@前加...号表示
     *
     * @param email 输入邮箱
     * @return 没有超过24位直接返回，超过24位，返回24位邮箱在@前加...号表示
     */
    public static String getEmaiStringlimit24(String email) {
        if (email.length() > 24) {
            try {
                int index = email.lastIndexOf("@");
                if (index > 0) {
                    String str1 = email.substring(index, email.length());
                    String str2 = email.substring(0, index);
                    int length = 24 - str1.length() - 3;
                    return str2.substring(0, length) + "..." + str1;
                } else {
                    return email.substring(0, 24);
                }
            } catch (StringIndexOutOfBoundsException exception) {
                exception.printStackTrace();
                return email.substring(0, 24);
            }
        } else {
            return email;
        }
    }



    /**
     * 是否是只需要弹窗的错误码
     *
     * @param errorCode webAPI专用
     * @return
     */
    public static boolean isTostCmd(String errorCode) {
        switch (errorCode) {
            case com.p2p.core.P2PSpecial.HttpErrorCode.ERROR_10000:
            case com.p2p.core.P2PSpecial.HttpErrorCode.ERROR_10001:
            case com.p2p.core.P2PSpecial.HttpErrorCode.ERROR_10901061:
            case com.p2p.core.P2PSpecial.HttpErrorCode.ERROR_10901060:
                return true;
            default:
                return false;
        }
    }

    public static <T extends HttpResult> boolean isTostCmd(T t) {
        switch (t.getError_code()) {
            case com.p2p.core.P2PSpecial.HttpErrorCode.ERROR_10000:
            case com.p2p.core.P2PSpecial.HttpErrorCode.ERROR_10001:
            case com.p2p.core.P2PSpecial.HttpErrorCode.ERROR_10901061:
            case HttpErrorCode.ERROR_10901060:
                return true;
            default:
                return false;
        }
    }

    /**
     * 特殊处理的服务器反馈码
     *
     * @param errorCode 错误码
     * @param msg       错误信息
     * @return 用户看到的提示信息
     */
    public static String GetToastCMDString(String errorCode, String msg) {
        switch (errorCode) {
            case HttpErrorCode.ERROR_10001:
            case HttpErrorCode.ERROR_10901061:
                return msg + "(" + errorCode + ")";
            case HttpErrorCode.ERROR_10000:
            case HttpErrorCode.ERROR_10901060:
                return "系统异常";
            default:
                return String.valueOf(errorCode);
        }
    }

    /**
     * 特殊处理的服务器反馈码
     *
     * @param t            服务器请求结果
     * @param <T>服务器请求结果父类
     * @return 用户看到的提示信息
     */
    public static <T extends HttpResult> String GetToastCMDString(T t) {
        switch (t.getError_code()) {
            case HttpErrorCode.ERROR_10001:
            case HttpErrorCode.ERROR_10901061:
                return t.getError() + "(" + t.getError_code() + ")";
            case HttpErrorCode.ERROR_10000:
            case HttpErrorCode.ERROR_10901060:
                return "系统异常";
            default:
                return String.valueOf(t.getError_code());
        }
    }

    /**
     * 生成随机密码
     *
     * @param pwdType 生成随机密码类型(0是主人非0是访客)
     * @return 生成的随机字母密码与随机数字密码 String[0]为随机字母密码，String[1]为随机数字密码
     */
    public static String[] createRandomPassword(int pwdType) {
        String prePwd = pwdType == 0 ? "master" : "visitor";
        String time = String.valueOf(System.currentTimeMillis());
        String proPwd = getRandomString(8);
        String UserPwd = prePwd + time + proPwd;
        String contactPwd = P2PHandler.getInstance().EntryPassword(UserPwd);
        return new String[]{UserPwd, contactPwd};
    }

    /**
     * 产生随机字符串
     *
     * @param length 随机字符串长度
     * @return 随机字符串
     */
    public static String getRandomString(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    /**
     * 判断是不是5Gwifi
     *
     * @param frequency
     * @return
     */
    public static boolean is5GWifi(int frequency) {
        String str = String.valueOf(frequency);
        if (str.length() > 0) {
            char a = str.charAt(0);
            if (a == '5') {
                return true;
            }
        }
        return false;
    }

    /**
     * WiFi是否加密
     *
     * @param result
     * @return
     */
    public static boolean isWifiOpen(ScanResult result) {
        return !(result.capabilities.toLowerCase().indexOf("wep") != -1
                || result.capabilities.toLowerCase().indexOf("wpa") != -1);
    }

    public static int dip2px(Context context, int dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
