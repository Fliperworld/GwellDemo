package entity;

import android.text.TextUtils;
import android.util.Log;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lele on 2018/5/2.
 */

public class QRcodeScanNetworkMode implements Serializable {
    private static final String URL = "http://yoosee.co/?";
    public static final String KEY_D = "D";
    private static String NETWORK_MODE = "(D=[0-9]{1,20}-[0-9]{1,9}-([0-9]|[A-F]){1,4})";
    private static final String SPLITE = "-";
    private static final String SPLITE2 = "=";
    /**
     * 配网方式结束位（现在只解析从0~2位，以后如果添加配网方式，需要改变这个解析的结束位）
     * 44版新增  0智能联机(7601)  1声波配网(海思联永国科)  2AP配网(8188)  3SimpleConfig  4蓝牙  5扫码
     */
    private static final int END_NETEORK_MODE_INDEX = 5;
    /**
     * 设备ID号
     */
    private String deviceID = "";
    /**
     * 序列号
     */
    private String serialNumber = "";
    /**
     * 是否选择配网方式的二维码
     */
    boolean isNetworkModeQRcode = false;
    /**
     * 是否是app已经有的配网方式
     */
    boolean isAlreadyNetworkMode = false;
    String url;
    /**
     * 配网方式
     */
    int networkMode = 0;

    public QRcodeScanNetworkMode(String url) {
        this.url = url;
        initData();
    }

    private void initData() {
        if (!TextUtils.isEmpty(url) && url.startsWith(URL)) {
            Pattern mPattern = Pattern.compile(NETWORK_MODE);
            Matcher mMatcher = mPattern.matcher(url);
            if (mMatcher.find()) {
                String deviceInfo = mMatcher.group(1);
                int index = deviceInfo.indexOf(SPLITE2);
                deviceInfo = deviceInfo.substring(index + 1, deviceInfo.length());
                String[] deviceInfos = deviceInfo.split(SPLITE);
                if (deviceInfo.length() >= 3) {
                    isNetworkModeQRcode = true;
                    serialNumber = deviceInfos[0];
                    deviceID = deviceInfos[1];
                    initNetworkMode(Integer.parseInt(deviceInfos[2], 16));
                }
            }
        }

    }

    //Smartlink，声波，AP，Simpeconfig，蓝牙
    public void initNetworkMode(int mode) {
        for (int i = END_NETEORK_MODE_INDEX; i >= 0; i--) {
            if ((mode >> i & 0x1) == 1) {
                networkMode = i;
                break;
            }
        }
        Log.e("QRCodeScan", "networkMode = " + networkMode);
        if (networkMode == NetworkMode.SMARTLINK || networkMode == NetworkMode.SOUND_WAVE ||
                networkMode == NetworkMode.AP || networkMode == NetworkMode.SIMPECONFIG ||
                networkMode == NetworkMode.SCAN) {
            isAlreadyNetworkMode = true;
        } else {
            isAlreadyNetworkMode = false;
        }
    }

    /**
     * 是否选择配网方式的二维码
     *
     * @return
     */
    public boolean isNetworkModeQRcode() {
        return isNetworkModeQRcode;
    }

    /**
     * 是否是app已经有的配网方式
     *
     * @return
     */
    public boolean isAlreadyNetworkMode() {
        return isAlreadyNetworkMode;
    }


    public String getDeviceID() {
        return deviceID;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public int getNetworkMode() {
        return networkMode;
    }

    public class NetworkMode {
        /**
         * smartlink配网
         */
        public static final int SMARTLINK = 0;
        /**
         * 声波配网
         */
        public static final int SOUND_WAVE = 1;
        /**
         * AP配网
         */
        public static final int AP = 2;
        /**
         * Simpeconfig配网
         */
        public static final int SIMPECONFIG = 3;
        /**
         * 蓝牙配网
         */
        public static final int BLUETOOTH = 4;
        /**
         * 扫码配网
         */
        public static final int SCAN = 5;

    }


}
