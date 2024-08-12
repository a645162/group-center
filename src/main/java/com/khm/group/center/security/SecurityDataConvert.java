package com.khm.group.center.security;

public class SecurityDataConvert {

    public static String bytesToHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    public static String bytesToBase64Str(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        return java.util.Base64.getEncoder().encodeToString(bytes);
    }

}
