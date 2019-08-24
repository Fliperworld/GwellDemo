package entity;

import android.text.TextUtils;

import java.io.Serializable;

/**
 * Created by lele on 2017/7/24.
 */

public class ShareUrlEntity implements Serializable{
    public static final String KEY_INVITECODE="InviteCode";
    public static final String KEY_DEVICEID="DeviceID";
    public static final String KEY_SHAREERNAME="SharerName";
    public static final String KEY_TYPE="Type";
    private String url="";
    private String InviteCode="";
    private String SharerName="";
    private String deviceId="";
    private String Type="";
    private String shareId="";

    public ShareUrlEntity(){

    }

    public ShareUrlEntity(String url) {
        this.url = url;
        analyzeUrl();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getInviteCode() {
        return InviteCode;
    }

    public void setInviteCode(String inviteCode) {
        InviteCode = inviteCode;
    }

    public String getSharerName() {
        return SharerName;
    }

    public void setSharerName(String sharerName) {
        SharerName = sharerName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getShareId() {
        return shareId;
    }

    public void setShareId(String shareId) {
        this.shareId = shareId;
    }

    @Override
    public String toString() {
        return "ShareUrlEntity{" +
                "url='" + url + '\'' +
                ", InviteCode='" + InviteCode + '\'' +
                ", SharerName='" + SharerName + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", Type='" + Type + '\'' +
                ", shareId='" + shareId + '\'' +
                '}';
    }

    /***
     * 获取url 指定name的value;
     * @param url
     * @param name
     * @return
     */
    public String getUrlValueByName(String url, String name) {
        String result = "";
        int index = url.indexOf("?");
        String temp = url.substring(index + 1);
        String[] keyValue = temp.split("&");
        for (String str : keyValue) {
            if (str.contains(name)) {
                result = str.replace(name + "=", "");
                break;
            }
        }
        return result;
    }

    public void analyzeUrl(){
        int index = url.lastIndexOf("?");
        String temp = url.substring(index + 1);
        String[] keyValue = temp.split("&");
        for (String str : keyValue) {
            if (str.contains(KEY_INVITECODE)) {
                InviteCode = str.replace(KEY_INVITECODE + "=", "");
                try {
                    shareId="0"+String.valueOf(Long.parseLong(InviteCode)&0xFFFFFFF);
                }catch (NumberFormatException e){
                    e.printStackTrace();
                }
            }
            if (str.contains(KEY_DEVICEID)) {
                deviceId = str.replace(KEY_DEVICEID + "=", "");
            }
            if (str.contains(KEY_SHAREERNAME)) {
                SharerName = str.replace(KEY_SHAREERNAME + "=", "");
            }
            if(str.contains(KEY_TYPE)){
                Type=str.replace(KEY_TYPE + "=", "");
            }
        }
    }

    /**
     * 是否是分享链接
     * @return
     */
    public boolean isShareUrl(){
        //Type==1代表是分享链接
        if(!TextUtils.isEmpty(Type)&&Type.equals("1")&&!TextUtils.isEmpty(InviteCode)&&!TextUtils.isEmpty(SharerName)&&!TextUtils.isEmpty(deviceId)){
            return true;
        }else {
            return false;
        }
    }
}
