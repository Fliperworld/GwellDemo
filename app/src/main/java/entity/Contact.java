package entity;

import android.text.TextUtils;
import com.p2p.core.P2PValue;
import com.p2p.core.global.P2PConstants;
import com.p2p.core.permission.Permission;

import java.io.Serializable;
import java.net.InetAddress;

import Utils.Util;

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
    //AP模式下的wifi密码
    public int mode = P2PValue.DeviceMode.GERNERY_MODE;
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
    //ACK返回结果
    private int ackFlag = P2PConstants.ACK_RET_TYPE.ACK_UNKNOWN;
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

    public int sceneMode = P2PValue.SceneMode.MODE_NONE;
    public long offlineTime = -1; //上次离线时间，app在线时从p2p库获取，以秒为单位
    public int lastSceneMode = P2PValue.SceneMode.MODE_NONE;

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

    public String getContactId() {
        if (mode == P2PValue.DeviceMode.AP_MODE) {
            return "1";
        } else {
            return contactId;
        }
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

    public String getPassword() {
        if (contactPassword == null || !Util.isNumeric(contactPassword)) {
            return "0";
        } else {
            return contactPassword;
        }


    }

    /**
     * 返回真实设备ID
     *
     * @return 设备ID
     */
    public String getRealContactID() {
        return contactId;
    }


    public boolean isPanorama() {
        return subType == P2PValue.subType.IPC_PANOMA_180_720
                || subType == P2PValue.subType.IPC_PANOMA_180_960
                || subType == P2PValue.subType.IPC_PANOMA_360_720
                || subType == P2PValue.subType.IPC_PANOMA_360_960;
    }

    public boolean is360Panorama() {
        if (isPanorama()) {
            return subType == P2PValue.subType.IPC_PANOMA_360_720
                    || subType == P2PValue.subType.IPC_PANOMA_360_960;
        }
        return false;
    }

    public boolean is180Panorama() {
        if (isPanorama()) {
            return subType == P2PValue.subType.IPC_PANOMA_180_720
                    || subType == P2PValue.subType.IPC_PANOMA_180_960;
        }
        return false;
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

    public int getAckFlag() {
        return ackFlag;
    }

    public void setAckFlag(int ackFlag) {
        this.ackFlag = ackFlag;
    }

    /** 获取设备热点网络下设备的ip地址
     * 非单机模式下直接返回0
     * @return
     */
    public int getDeviceIp() {
        if (mode == P2PValue.DeviceMode.AP_MODE) {
            return Util.ipToIntValue(ipadressAddress);
        } else {
            return 0;
        }
    }


    /**
     * 是否访客密码
     *
     * @return
     */
    public boolean isVisitor() {
        return ackFlag == P2PConstants.ACK_RET_TYPE.ACK_INSUFFICIENT_PERMISSIONS;
    }

    /**
     * 是否错误密码
     *
     * @return
     */
    public boolean isWrongPassword() {
        return ackFlag == P2PConstants.ACK_RET_TYPE.ACK_PWD_ERROR;
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
     * 是否有function功能的权限
     *
     * @param function {@link com.p2p.core.permission.Permission}
     */
    public boolean hasPermission(int function) {
        if (function == Permission.RECIEVE_DEVICE_MSG) {
            //接收设备信息权限
            if ((permission & 0x1) == 0) {
                //旧的添加方式拥有所有权限
                return true;
            } else {
                //主人和访客的接收设备信息权限由权限位表示
                return ((permission >> function & 0x1) == 1);
            }
        } else {
            //其它权限
            if ((permission & 0x1) == 0) {
                //旧的添加方式拥有所有权限
                return true;
            } else if ((permission >> 1 & 0x1) == 1) {
                //主人拥有所有权限
                return true;
            } else {
                //访客
                return ((permission >> function & 0x1) == 1);
            }
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

    /**
     * 根据设备主类型和子类型判断设备是否可以摇头
     *
     * @return
     */
    public boolean isSupportShakeHead() {
        //门铃和车库灯不支持摇头
        if (contactType == P2PValue.DeviceType.DOORBELL || isSupportFunction(P2PValue.DeviceConfigFunction.INTELLIGHT_GARAGE_LIGHT)) {
            return false;
        }
        if (contactType == P2PValue.DeviceType.IPC) {
            // 不支持摇头的子类型：1（720P 卡片机）,2（720P 38板）,3（720P 门铃）,11（960P 卡片机）,12（960P 38板）,13（960P 门铃）,21（1080P 卡片机）,22（1080P 38板）,23（1080P 门铃）,33（130W  360度全景）,34（100W 180度全景）,35（200W/300W 360度全景）,36（200W 180度全景）
            if (subType == P2PValue.subType.IPC_DEV_SUB_TYPE_SIMPLE
                    || subType == P2PValue.subType.IPC_DEV_SUB_TYPE_38X38
                    || subType == P2PValue.subType.IPC_DEV_SUB_TYPE_DOORBELL
                    || subType == P2PValue.subType.IPC_DEV_SUB_TYPE_130W_SIMPLE
                    || subType == P2PValue.subType.IPC_DEV_SUB_TYPE_130W_38X38
                    || subType == P2PValue.subType.IPC_DEV_SUB_TYPE_130W_DOORBELL
                    || subType == P2PValue.subType.IPC_DEV_SUB_TYPE_200W_SIMPLE
                    || subType == P2PValue.subType.IPC_DEV_SUB_TYPE_200W_38X38
                    || subType == P2PValue.subType.IPC_DEV_SUB_TYPE_200W_DOORBELL
                    || subType == P2PValue.subType.IPC_PANOMA_360_720
                    || subType == P2PValue.subType.IPC_PANOMA_180_720
                    || subType == P2PValue.subType.IPC_PANOMA_360_960
                    || subType == P2PValue.subType.IPC_PANOMA_180_960) {
                return false;
            }
        }
        return true;
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
                ", mode=" + mode +
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
