package entity;

import android.text.TextUtils;

import org.json.JSONObject;

import java.io.Serializable;

import it.sauronsoftware.base64.Base64;

/**
 * Created by lele on 2017/11/16.
 */

public class QRcodeAddDevice implements Serializable {
    private static final String DEVICEID = "DevId";
    private static final String DEVICETYPE = "DevType";
    private static final String DEVICEPWD = "Password";
    String result;
    int deviceType;
    String deviceId="";
    String devicePwd="";
    /**
     * 是否二维码添加设备
     */
    boolean isQRcodeAddDevice=false;


    public QRcodeAddDevice(String result) {
        this.result = result;
        init();
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDevicePwd() {
        return devicePwd;
    }

    public void setDevicePwd(String devicePwd) {
        this.devicePwd = devicePwd;
    }

    public boolean isQRcodeAddDevice() {
        return isQRcodeAddDevice;
    }

    public void setQRcodeAddDevice(boolean QRcodeAddDevice) {
        isQRcodeAddDevice = QRcodeAddDevice;
    }

    private void init() {
        if (!TextUtils.isEmpty(result)) {
            try {
                String deleteFirst = result.substring(result.indexOf("#") + 1);
                String json = Base64.decode(deleteFirst);
                paserJson(json);
            } catch (Exception e) {

            }
        }
    }

    private void paserJson(String json) throws Exception {
        JSONObject obj = new JSONObject(json);
        if (!json.contains(DEVICEID)) {
            throw new Exception("json not contains deviceid");
        }
        deviceType = obj.getInt(DEVICETYPE);
        deviceId= obj.getString(DEVICEID);
        if (json.contains(DEVICEPWD)) {
            devicePwd = obj.getString(DEVICEPWD);
            isQRcodeAddDevice=true;
        }
    }
}
