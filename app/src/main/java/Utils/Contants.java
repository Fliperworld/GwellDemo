package Utils;

/**
 * Created by Administrator on 2017/3/14.
 */
public class Contants {

    public static final String P2P_CONNECT ="com.example.dansesshou."+"P2P_CONNECT";
    public static final String PACKAGE_NAME = "com.gwelldemo.";

    public static class P2P {
        public static final String RET_SET_INIT_PASSWORD = PACKAGE_NAME
                + "RET_SET_INIT_PASSWORD";
        // 检查密码
        public static final String ACK_RET_CHECK_PASSWORD = PACKAGE_NAME
                + "ACK_RET_CHECK_PASSWORD";
        public static final String RET_SET_VISITOR_DEVICE_PASSWORD = PACKAGE_NAME
                + "RET_SET_VISITOR_DEVICE_PASSWORD";
        // 设置访客密码
        public static final String ACK_RET_SET_VISITOR_DEVICE_PASSWORD = PACKAGE_NAME
                + "ACK_RET_SET_VISITOR_DEVICE_PASSWORD";
        // 修改设备密码相关
        public static final String ACK_RET_SET_DEVICE_PASSWORD = PACKAGE_NAME
                + "ACK_RET_SET_DEVICE_PASSWORD";
        public static final String RET_SET_DEVICE_PASSWORD = PACKAGE_NAME
                + "RET_SET_DEVICE_PASSWORD";
    }

    public static class P2P_SET {

        public static class INIT_PASSWORD_SET {
            public static final int SETTING_SUCCESS = 0;
            public static final int ALREADY_EXIST_PASSWORD = 43;
        }

        public static class DEVICE_VISITOR_PASSWORD_SET {
            public static final int SETTING_SUCCESS = 0;
        }

        public static class ACK_RESULT {
            public static final int ACK_PWD_ERROR = 9999;
            public static final int ACK_NET_ERROR = 9998;
            public static final int ACK_SUCCESS = 9997;
            public static final int ACK_INSUFFICIENT_PERMISSIONS = 9996;
            public static final int ACK_DEVICE_OFFLINE = 9995;
        }

        public static class DEVICE_PASSWORD_SET {
            public static final int SETTING_SUCCESS = 0;
        }


    }

}
