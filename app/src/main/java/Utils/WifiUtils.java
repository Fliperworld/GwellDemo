package Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

import sdk.MyApp;

public class WifiUtils {
    public final static int WIFICIPHER_WEP = 1;
    public final static int WIFICIPHER_WPA = 2;
    public final static int WIFICIPHER_NOPASS = 0;
    public final static int WIFICIPHER_INVALID = 4;

    private static WifiUtils wifiutils = null;
    private WifiManager wifiManager;
    private Context context;
    private Handler mHandler;
    private static String APTag = "GW_IPC_";
    private WifiLock wifiLock;

    // 构造函数
    private WifiUtils(WifiManager wifiManager) {
        this.wifiManager = wifiManager;
    }

    private WifiUtils(Context context) {
        this.context = context;
        this.wifiManager = (WifiManager) MyApp.app
                .getSystemService(Context.WIFI_SERVICE);
    }

    public static WifiUtils getInstance() {
        if (wifiutils == null) {
            wifiutils = new WifiUtils(MyApp.app);
        }
        if (wifiutils.wifiManager == null) {
            wifiutils.wifiManager = (WifiManager) MyApp.app
                    .getSystemService(Context.WIFI_SERVICE);
        }
        return wifiutils;
    }

    /**
     * 创建WiFi锁
     *
     * @param lockName
     * @return
     */
    public WifiLock CreatWifiLock(String lockName) {
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY,
                lockName);
        return wifiLock;
    }

    public void wifiLock() {
        if (wifiLock == null) {
            CreatWifiLock(APTag);
        }
        wifiLock.acquire();
    }

    public void wifiUnlock() {
        if (wifiLock != null && wifiLock.isHeld()) {
            wifiLock.release();
        }
    }

    public void setHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    public boolean getIsOpen() {
        return wifiManager.isWifiEnabled();
    }

    public void connectWifi(String SSID, String Password, int Type) throws Exception {
        if (wifiManager != null) {
            if (getIsOpen()) {
                ConnectHandler(SSID, Password, Type);
            } else {
                // 打开WIFI
                if (OpenWifi()) {
                    ConnectHandler(SSID, Password, Type);
                } else {
                    // 打开Wifi失败
                    throw new Exception("open wifi fail");
                }
            }
        }
    }

    // 打开wifi功能
    public boolean OpenWifi() {
        boolean bRet = true;
        if (!wifiManager.isWifiEnabled()) {
            bRet = wifiManager.setWifiEnabled(true);
        }
        return bRet;
    }

    public boolean ConnectHandler(String SSID, String Password, int Type) throws Exception {
        //wifiManager.disconnect();
        WifiConfiguration wifi = this.IsExsits(SSID);
        int currentapiVersion = Build.VERSION.SDK_INT;
        if (wifi == null) {
            WifiConfiguration wifiConfig = this.CreateWifiInfo(SSID, Password, Type);
            if (wifiConfig != null) {
                int netID = wifiManager.addNetwork(wifiConfig);
                if (netID >= 0) {
                    if (!connectWifi(netID)) {
                        throw new Exception(SSID + "Is not exsits and connectWifi error return :" + netID);
                    }
                } else {
                    throw new Exception(SSID + "Is not exsits addNetwork error return :" + netID);
                }
            } else {
                throw new Exception(SSID + "Is not exsits CreateWifiInfo error return null");
            }
            return true;
        } else {
            if (currentapiVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
                if (!connectWifi(wifi.networkId)) {
                    throw new Exception(SSID + "exsist and connectWifi error return :" + wifi.networkId);
                }
                return true;
            } else {
                boolean isremove = wifiManager.removeNetwork(wifi.networkId);
                WifiConfiguration wifiConfig = this
                        .CreateWifiInfo(SSID, Password, Type);
                if (wifiConfig != null) {
                    int netID = wifiManager.addNetwork(wifiConfig);
                    if (netID >= 0) {
                        if (!connectWifi(netID)) {
                            throw new Exception(SSID + "Exsits and connectWifi error return :" + netID);
                        }
                    } else {
                        throw new Exception(SSID + "Exsits addNetwork error return :" + netID);
                    }
                } else {
                    throw new Exception(SSID + "Exsits CreateWifiInfo error return null");
                }
                return true;
            }
        }
    }


    public void removeWifi(String ssid) {
        WifiConfiguration wifi = this.IsExsits(ssid);
        if (wifi != null) {
            boolean isRemove = wifiManager.removeNetwork(wifi.networkId);
            wifiManager.saveConfiguration();
        }
    }

    /**
     * 链接指定wifi
     **/
    public boolean connectWifi(int netId) {
        boolean connectResult = wifiManager.enableNetwork(netId, true);
        Log.e("dxsTest", "connectResult=" + connectResult);
        if (connectResult) {
            wifiManager.saveConfiguration();
            wifiManager.reconnect();
        }
        return connectResult;

    }

    // 查看以前是否也配置过这个网络
    public WifiConfiguration IsExsits(String SSID) {
        if (SSID == null || wifiManager == null) {
            return null;
        }
        List<WifiConfiguration> existingConfigs = wifiManager
                .getConfiguredNetworks();
        if (existingConfigs == null) {
            return null;
        }
        WifiConfiguration wifiConfiguration = null;
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")
                    || existingConfig.SSID.equals(SSID)) {
                wifiConfiguration = existingConfig;
                return existingConfig;
            }
        }
        return wifiConfiguration;
    }

    public WifiConfiguration CreateWifiInfo(String SSID, String Password,
                                            int Type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        boolean has = false;
        try {
            has = containsTest();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        if(has){
        config.SSID = "\"" + SSID + "\"";
//        }else{
//            config.SSID = SSID;
//        }
        Log.e("dxsTest", "CreateWifiInfo has:" + has + "---config.SSID:" + config.SSID);
        //config.SSID = "\"" + SSID + "\"";
        if (Type == WIFICIPHER_NOPASS) {
            config.wepKeys[0] = "\"" + "\"";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == WIFICIPHER_WEP) {
//			config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + Password + "\"";
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == WIFICIPHER_WPA) {
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            if (Password == null || Password.equals("")) {
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                // config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.NONE);
            } else {
                config.preSharedKey = "\"" + Password + "\"";
                config.allowedGroupCiphers
                        .set(WifiConfiguration.GroupCipher.TKIP);
                config.allowedKeyManagement
                        .set(WifiConfiguration.KeyMgmt.WPA_PSK);
                config.allowedPairwiseCiphers
                        .set(WifiConfiguration.PairwiseCipher.TKIP);
                // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                config.allowedGroupCiphers
                        .set(WifiConfiguration.GroupCipher.CCMP);
                config.allowedPairwiseCiphers
                        .set(WifiConfiguration.PairwiseCipher.CCMP);
            }
            config.status = WifiConfiguration.Status.ENABLED;
        } else {
            return null;
        }
        return config;
    }

    // 检查指定的wifi是否存在列表中
    public boolean isScanExist(String SSid) {

        List<ScanResult> lists = getLists();
        if (lists != null && lists.size() > 0) {

            for (int i = 0; i < lists.size(); i++) {

                if (lists.get(i).SSID.equals(SSid)) {

                    return true;
                }
            }

        }
        return false;
    }

    public List<ScanResult> getLists() {
        wifiManager.startScan();
        List<ScanResult> lists = wifiManager.getScanResults();
        return lists;
    }

    public boolean isConnectWifi(String SSID) {
        if (wifiManager == null) {
            return false;
        }
        String connectSSID = wifiManager.getConnectionInfo().getSSID();
        int workId = wifiManager.getConnectionInfo().getNetworkId();
        if (TextUtils.isEmpty(connectSSID) || workId == -1) {
            return false;
        }
        return connectSSID.equals("\"" + SSID + "\"")
                || connectSSID.equals(SSID);
    }

    public void SetWiFiEnAble(boolean enable) {
        if (wifiManager != null) {
            wifiManager.setWifiEnabled(enable);
        }
    }

    /**
     * 仅断开wifi，并不忘记网络
     *
     * @param ssid wifi名
     */
    public void DisConnectWifi(String ssid) {
        if (isConnectWifi(ssid)) {
            wifiManager.disconnect();
        }
    }

    /**
     * 断开某个WiFi，并忘记网络
     *
     * @param ssid
     */
    public void disConnectWifi(String ssid) {
        if (isConnectWifi(ssid)) {
            boolean isdisconnect=wifiManager.disconnect();
        }
        WifiConfiguration wifi = this.IsExsits(ssid);
        if (wifi != null) {
            boolean result = wifiManager.removeNetwork(wifi.networkId);
        } else {
        }
    }

    public String getConnectWifiName() {
        if (!isWifiConnected(MyApp.app)) {
            return "";
        }
        WifiManager wifiManager = (WifiManager) MyApp.app.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            return "";
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null) {
            return "";
        }
        String ssid = wifiInfo.getSSID();
        if (ssid.length() <= 0) {
            return "";
        }
        int a = ssid.charAt(0);
        if (a == 34) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        return ssid;

    }

    public WifiInfo getConnectWifiInfo() {
        if (!isWifiConnected(MyApp.app)) {
            return null;
        }
        WifiManager wifiManager = (WifiManager) MyApp.app.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            return null;
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo;
    }

    /**
     * 判断是否连接上wifi
     *
     * @param context
     * @return
     */
    public static boolean isWifiConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWiFiNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWiFiNetworkInfo != null) {
                if (mWiFiNetworkInfo.isAvailable()) {
                    return mWiFiNetworkInfo.isConnected();
                }
                return false;
            }
        }
        return false;
    }

    /**
     * 判断是否是移动数据连接
     *
     * @param context
     * @return
     */
    public static boolean isMobileConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mMobileNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mMobileNetworkInfo != null) {
                if (mMobileNetworkInfo.isAvailable()) {
                    return mMobileNetworkInfo.isConnected();
                }
                return false;
            }
        }
        return false;
    }

    public boolean containsTest() throws Exception {
        List<ScanResult> list = getLists();
        for (ScanResult result : list) {
            Log.e("dxsTest", "SSID:" + result.SSID);
            return result.SSID.contains("\"");
        }
        throw new Exception("containsTest no wifi info");
    }

    /**
     * 是否连接上网络
     *
     * @return
     */
    public boolean isConnectNetwork() {
        ConnectivityManager mConnectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        TelephonyManager mTelephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        //检查网络连接
        NetworkInfo info = mConnectivity.getActiveNetworkInfo();

        if (info == null || !mConnectivity.getBackgroundDataSetting()) {
            return false;
        }
        return true;

    }


}
