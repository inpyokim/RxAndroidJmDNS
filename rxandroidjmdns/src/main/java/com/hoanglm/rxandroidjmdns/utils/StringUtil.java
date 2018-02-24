package com.hoanglm.rxandroidjmdns.utils;

import java.io.UnsupportedEncodingException;

public class StringUtil {

    public static String convertByteToString(byte[] data) {
        try {
            return new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            RxJmDNSLog.e(e, "convertByteToString>>");
            return "";
        }
    }

    public static byte[] convertStringToByte(String data) {
        try {
            return data.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            RxJmDNSLog.e(e, "convertStringToByte>>");
            return new byte[0];
        }
    }
}
