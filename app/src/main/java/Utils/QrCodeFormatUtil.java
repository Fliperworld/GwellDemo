package Utils;

public class QrCodeFormatUtil {
    public static String toHexString(int data) {
        String s = Integer.toHexString(data);
        if (s.length() == 1) {
            return "0"+s;
        }else {
            return s;
        }
    }
}
