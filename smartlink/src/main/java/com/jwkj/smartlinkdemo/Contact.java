package com.jwkj.smartlinkdemo;

import android.text.TextUtils;

import java.io.Serializable;
import java.net.InetAddress;

public class Contact implements Serializable, Comparable<Contact> {

    // id
    public int id;
    // 联系人名称
    public String contactName = "";
    // 联系人ID
    public String contactId;
    // 联系人监控密码 注意：不是登陆密码，只有当联系人类型为设备才有
    public String contactPassword = "0";
    // 联系人类型
    public int contactType;
    // 此联系人发来多少条未读消息
    public int messageCount;
    // 当前登录的用户
    public String activeUser = "";

    // ip地址
    public InetAddress ipadressAddress;
    // 用户输入的密码
    public String userPassword = "";
    //当前版本
    public String cur_version = "";
    //可更新到的版本
    public String up_version = "";
    //有木有rtsp标记
    public int rtspflag = 0;
    // 按在线状态排序
    public String wifiPassword = "";

    public boolean isConnectApWifi = false;
    public int subType = 0;
    public int FishMode = -1;
    public int videow = 896;
    public int videoh = 896;
    public int fishPos = 0;
    //好友ID属性,第一个Bit不为0 , 则表示该库支持Index服务器
    public int IdProperty;
    //设备属性修改时间
    private String modifyTime;
    //设备是否已经删除，1正常，0表示删除
    private int dropFlag = 1;//默认是1
    //布撤防标记
    private long defenceFlag;

    //权限
    private int permission = 0;
    /**
     * 设备客户ID
     */
    private int customId = 0;
    /**
     * 设备MAC地址
     */
    private String mac;

    /**
     * 全景设备画面剪切比率，大小：0-100,默认100
     */
    private int cutRatio = 100;
    /**
     * 全景设备画面中心x相对于画面宽的比值*1000，大小：0-1000，默认：500；
     */
    private int cutXValue = 500;
    /**
     * 全景设备画面中心y相对于画面高的比值*1000，大小：0-1000，默认：500；
     */
    private int cutYValue = 500;
    //云报警服务截止时间
    private String alarmDeadline = "";
    //报警接收手机号
    private String alarmPhone = "";
    private String storageDeadline = "";
    private String liveDeadline = "";

    /**
     * 权限管理（0:设备不支持权限管理功能，1：设备支持权限管理功能）
     */
    private int supportPermissionManage;
    /**
     * 开启权限管理（0:设备没有开启权限管理功能，1：设备已经开启权限管理功能）
     */
    private int startPermissionManage;
    /**
     * index服务器返回的功能选项
     */
    private int configFunction;

    /**
     * index服务器返回的功能选项2
     */
    private int configFunction2;

    private int p2pLibVersion;

    @Override
    public int compareTo(Contact arg0) {
        // TODO Auto-generated method stub
        //排序优先级：在线 弱密码 ID号
        Contact o = arg0;
        if (Integer.parseInt(o.contactId) < Integer.parseInt(this.contactId)) {
            return 1;
        } else if (Integer.parseInt(o.contactId) > Integer.parseInt(this.contactId)) {
            return -1;
        }
        return 0;
    }

    /**
     * 获取设备IP最后一段
     *
     * @return 空返回""
     */
    public String getIpMark() {
        if (ipadressAddress != null) {
            String mark = ipadressAddress.getHostAddress();
            return mark.substring(mark.lastIndexOf(".") + 1, mark.length());
        }
        return "";
    }

    public String getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(String modifyTime) {
        this.modifyTime = modifyTime;
    }

    public int getDropFlag() {
        return dropFlag;
    }

    public void setDropFlag(int dropFlag) {
        this.dropFlag = dropFlag;
    }

    public long getDefenceFlag() {
        return defenceFlag;
    }

    public void setDefenceFlag(int defenceFlag) {
        this.defenceFlag = defenceFlag;
    }

    public String getAlarmPhone() {
        return alarmPhone;
    }

    public void setAlarmPhone(String alarmPhone) {
        this.alarmPhone = alarmPhone;
    }

    public String getAlarmDeadline() {
        return alarmDeadline;
    }

    public void setAlarmDeadline(String alarmDeadline) {
        this.alarmDeadline = alarmDeadline;
    }

    public String getStorageDeadline() {
        return storageDeadline;
    }

    public void setStorageDeadline(String storageDeadline) {
        this.storageDeadline = storageDeadline;
    }

    public String getLiveDeadline() {
        return liveDeadline;
    }

    public void setLiveDeadline(String liveDeadline) {
        this.liveDeadline = liveDeadline;
    }

    public String getIpContactId() {
        String ip = getIpMark();
        if (!ip.equals("")) {
            return ip;
        }
        return contactId;
    }

    /**
     * 返回真实设备ID
     *
     * @return 设备ID
     */
    public String getRealContactID() {
        return contactId;
    }

    public int getIdProperty() {
        return IdProperty;
    }

    public void setIdProperty(int friendsIdProperty) {
        IdProperty = friendsIdProperty;
    }

    public boolean isSurpportIndex() {
        return (IdProperty & 0x1) == 1;
    }

    public int getSubType() {
        return subType;
    }

    public void setSubType(int subType) {
        this.subType = subType;
    }

    /**
     * 设备录像文件名需要保存的信息
     *
     * @return 部分文件名
     */
    public String getVideoInfo() {
        StringBuffer buffer = new StringBuffer();
        //分隔符由之前的|改为_
        buffer.append(contactId).append("_");
        buffer.append(contactType).append("_");
        buffer.append(subType).append("_");
        //buffer.append(FishMode).append("_");
        buffer.append(videow).append("_");
        buffer.append(videoh).append("_");
        buffer.append(fishPos);
        return buffer.toString();
    }

    public int getPermission() {
        return permission;
    }

    public void setPermission(int permission) {
        this.permission = permission;
    }

    public int getCustomId() {
        return customId;
    }

    public void setCustomId(int customId) {
        this.customId = customId;
    }

    public String getMac() {
        if (TextUtils.isEmpty(mac)) {
            return "000000000000";
        }
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    //是否有监控权限
    public boolean isCanMonitor() {
        if ((permission & 0x1) == 0) {
            return true;
        } else if ((permission >> 1 & 0x1) == 1) {
            return true;
        } else {
            if ((permission >> 2 & 0x1) == 0) {
                return false;
            } else {
                return true;
            }
        }
    }

    /**
     * 是否打开离线通知
     * permission Bit10 表示设备离线后是否开启接收离线通知（0：关闭离线通知，1：开启离线通知）
     */
    public boolean isOpenOfflineNotification() {
        if ((permission >> 10 & 0x1) == 1) {
            return true;
        } else {
            return false;
        }
    }


    public int getCutRatio() {
        return cutRatio;
    }

    public void setCutRatio(int cutRatio) {
        this.cutRatio = cutRatio;
    }

    public int getCutXValue() {
        return cutXValue;
    }

    public int getCutYValue() {
        return cutYValue;
    }

    public void setCutXValue(int cutXValue) {
        this.cutXValue = cutXValue;
    }

    public void setCutYValue(int cutYValue) {
        this.cutYValue = cutYValue;
    }

    /**
     * 是否支持某个功能（configFunction的第function位是否为1）
     *
     * @param function
     * @return
     */
    public boolean isSupportFunction(int function) {
        if (configFunction == -1) {
            //单机模式未获取到功能选项
            return false;
        } else {
            return (((configFunction >> function) & 0x1) == 1);
        }
    }

    /**
     * 是否支持某个功能（configFunction2的第function位是否为1）
     *
     * @param function
     * @return
     */
    public boolean isSupportFunction2(int function) {
        if (configFunction2 == -1) {
            //单机模式未获取到功能选项
            return false;
        } else {
            return (((configFunction2 >> function) & 0x1) == 1);
        }
    }

    /**
     * 是否有function其中一个功能的权限，只要有一个，就返回true（针对访客）
     *
     * @param function
     */
    public boolean hasOnePermission(int[] function) {
        for (int f : function) {
            if ((permission >> f & 0x1) == 1) {
                return true;
            }
        }
        return false;
    }


    public int getSupportPermissionManage() {
        return supportPermissionManage;
    }

    public void setSupportPermissionManage(int supportPermissionManage) {
        this.supportPermissionManage = supportPermissionManage;
    }

    public int getStartPermissionManage() {
        return startPermissionManage;
    }

    public void setStartPermissionManage(int startPermissionManage) {
        this.startPermissionManage = startPermissionManage;
    }

    /**
     * 是否支持权限管理
     *
     * @return
     */
    public boolean isSupportPermissionManage() {
        return (supportPermissionManage == 1);

    }

    /**
     * 是否开启权限管理
     *
     * @return
     */
    public boolean isStartPermissionManage() {
        return (startPermissionManage == 1);
    }

    public int getConfigFunction2() {
        return configFunction2;
    }

    public void setConfigFunction2(int configFunction2) {
        this.configFunction2 = configFunction2;
    }

    public void setP2pLibVersion(int version) {
        this.p2pLibVersion = version;
    }

    public int getP2pLibVersion() {
        return p2pLibVersion;
    }



    @Override
    public String toString() {
        return "Contact{" +
                " contactName='" + contactName + '\'' +
                ", contactId='" + contactId + '\'' +
                ", contactPassword='" + contactPassword + '\'' +
                ", contactType=" + contactType +
                ", activeUser='" + activeUser + '\'' +
                ", userPassword='" + userPassword + '\'' +
                ", cur_version='" + cur_version + '\'' +
                ", up_version='" + up_version + '\'' +
                ", rtspflag=" + rtspflag +
                ", wifiPassword='" + wifiPassword + '\'' +
                ", isConnectApWifi=" + isConnectApWifi +
                ", subType=" + subType +
                ", FishMode=" + FishMode +
                ", modifyTime='" + modifyTime + '\'' +
                ", dropFlag=" + dropFlag +
                ", permission=" + permission +
                ", configFunction=" + configFunction +
                ", supportPermissionManage=" + supportPermissionManage +
                ", startPermissionManage=" + startPermissionManage +
                ", p2pLibVersion=" + p2pLibVersion +
                '}';
    }
}
