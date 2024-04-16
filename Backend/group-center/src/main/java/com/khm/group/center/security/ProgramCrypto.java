package com.khm.group.center.security;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.Security;

import com.tencent.kona.crypto.KonaCryptoProvider;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class ProgramCrypto {

    private static final Logger logger =
            LoggerFactory.getLogger(ProgramCrypto.class);

    private static boolean isProviderAdded = false;

    private static void addProvider() {
        if (!isProviderAdded) {
            Security.addProvider(new KonaCryptoProvider());
            isProviderAdded = true;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // Hash SM3
    ///////////////////////////////////////////////////////////////////////////////////////////
    public static byte[] hashSM3(byte[] message) {
        if (message == null || message.length == 0) {
            return null;
        }

        addProvider();

        try {
            MessageDigest md = MessageDigest.getInstance("SM3");
            return md.digest(message);
        } catch (Exception e) {
            logger.error("Error in encrypting string with SM3 algorithm");
            logger.error(e.toString());
            return null;
        }
    }

    public static byte[] hashSM3(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }

        byte[] message = str.getBytes(StandardCharsets.UTF_8);
        return hashSM3(message);
    }

    public static String hashSM3HexStr(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }

        byte[] encryptedMessage = hashSM3(str);
        if (encryptedMessage == null) {
            return null;
        }

        return SecurityDataConvert.bytesToHex(encryptedMessage);
    }

    public static String hashSM3Base64Str(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }

        byte[] encryptedMessage = hashSM3(str);
        if (encryptedMessage == null) {
            return null;
        }

        return SecurityDataConvert.bytesToBase64Str(encryptedMessage);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // HmacSM3
    ///////////////////////////////////////////////////////////////////////////////////////////
    public static byte[] encryptHmacSM3(byte[] message, byte[] key) {
        if (message == null || message.length == 0 ||
                key == null || key.length == 0) {
            return null;
        }

        addProvider();

        try {
            SecretKey secretKey = new SecretKeySpec(key, "SM4");
            Mac hmac = Mac.getInstance("HmacSM3");
            hmac.init(secretKey);

            return hmac.doFinal(message);
        } catch (Exception e) {
            logger.error("Error in encrypting string with HmacSM3 algorithm");
            logger.error(e.toString());
            return null;
        }
    }

    public static byte[] encryptHmacSM3(String str, String keyStr) {
        if (str == null || str.trim().isEmpty() ||
                keyStr == null || keyStr.trim().isEmpty()) {
            return null;
        }

        byte[] message = str.getBytes(StandardCharsets.UTF_8);
        byte[] key = keyStr.getBytes(StandardCharsets.UTF_8);

        return encryptHmacSM3(message, key);
    }

    public static String encryptHmacSM3HexStr(String str, String keyStr) {
        if (str == null || str.trim().isEmpty() ||
                keyStr == null || keyStr.trim().isEmpty()) {
            return null;
        }

        byte[] encryptedMessage = encryptHmacSM3(str, keyStr);
        return SecurityDataConvert.bytesToHex(encryptedMessage);
    }

    public static String encryptHmacSM3Base64Str(String str, String keyStr) {
        if (str == null || str.trim().isEmpty() ||
                keyStr == null || keyStr.trim().isEmpty()) {
            return null;
        }

        byte[] encryptedMessage = encryptHmacSM3(str, keyStr);
        return SecurityDataConvert.bytesToBase64Str(encryptedMessage);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // JWT(HmacSM3)
    ///////////////////////////////////////////////////////////////////////////////////////////
    public static @NotNull String encryptJWTString(String str, String keyStr) {
        String header, payload;

        header = "{\"alg\":\"HmacSM3\",\"typ\":\"JWT\"}";
        payload = str;

        String originalText = header + "." + payload;

        String signatureBase64Str =
                encryptHmacSM3Base64Str(originalText, keyStr);

        // Base64编码
        String headerBase64Str = SecurityDataConvert.bytesToBase64Str(
                header.getBytes(StandardCharsets.UTF_8)
        );
        String payloadBase64Str = SecurityDataConvert.bytesToBase64Str(
                payload.getBytes(StandardCharsets.UTF_8)
        );

        // 组装
        return headerBase64Str + "." + payloadBase64Str + "." + signatureBase64Str;
    }

    public static String decryptJWTString(String jwtStr, String keyStr) {
        if (jwtStr == null || jwtStr.trim().isEmpty() ||
                keyStr == null || keyStr.trim().isEmpty()) {
            return null;
        }

        String[] jwtParts = jwtStr.trim().split("\\.");
        if (jwtParts.length != 3) {
            return null;
        }

        String headerBase64Str = jwtParts[0];
        String payloadBase64Str = jwtParts[1];
        String signatureBase64Str = jwtParts[2];

        // Base64解码
        byte[] headerBytes = java.util.Base64.getDecoder().decode(headerBase64Str);
        byte[] payloadBytes = java.util.Base64.getDecoder().decode(payloadBase64Str);

        String header = new String(headerBytes, StandardCharsets.UTF_8);
        String payload = new String(payloadBytes, StandardCharsets.UTF_8);

        String originalText = header + "." + payload;

        String signatureBase64Str2 =
                encryptHmacSM3Base64Str(originalText, keyStr);

        if (!signatureBase64Str.equals(signatureBase64Str2)) {
            return null;
        }

        return payload;
    }

}
