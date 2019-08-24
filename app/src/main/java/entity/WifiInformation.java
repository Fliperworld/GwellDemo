package entity;

import java.io.Serializable;

/**
 * Created by lele on 2018/4/24.
 * Wifi信息类
 */

public class WifiInformation implements Serializable {
    String wifiName;
    String wifiPwd;
    int encryptType;
    int mLocalIp;
    String mac;
    /*44版 AP 二维码生成 加密类型 0,1,2  SimepleConfig 加密 ..8, 9*/
    private int subEncryptType;

    public WifiInformation(String wifiName, String wifiPwd, int encryptType, int mLocalIp, String mac) {
        this.wifiName = wifiName;
        this.wifiPwd = wifiPwd;
        this.encryptType = encryptType;
        this.mLocalIp = mLocalIp;
        this.mac = mac;
    }

    public WifiInformation(String wifiName, String wifiPwd, int encryptType, int mLocalIp, String mac, int subEncryptType) {
        this(wifiName, wifiPwd, encryptType, mLocalIp, mac);
        this.subEncryptType = subEncryptType;
    }

    public String getWifiName() {
        return wifiName;
    }

    public void setWifiName(String wifiName) {
        this.wifiName = wifiName;
    }

    public String getWifiPwd() {
        return wifiPwd;
    }

    public void setWifiPwd(String wifiPwd) {
        this.wifiPwd = wifiPwd;
    }

    public int getEncryptType() {
        return encryptType;
    }

    public void setEncryptType(int encryptType) {
        this.encryptType = encryptType;
    }

    public WifiInformation(int mLocalIp) {
        this.mLocalIp = mLocalIp;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public int getSubEncryptType() {
        return subEncryptType;
    }

    public void setSubEncryptType(int subEncryptType) {
        this.subEncryptType = subEncryptType;
    }

    @Override
    public String toString() {
        return "WifiInformation{" +
                "wifiName='" + wifiName + '\'' +
                ", wifiPwd='" + wifiPwd + '\'' +
                ", encryptType=" + encryptType +
                ", mLocalIp=" + mLocalIp +
                ", mac='" + mac + '\'' +
                '}';
    }
}
