package com.khm.group.center.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.Security;

import com.tencent.kona.crypto.KonaCryptoProvider;

import static com.khm.group.center.security.SecurityDataConvert.bytesToHex;

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

    public static String encryptString(String str) {
        if (str == null || str.trim().isEmpty()) {
            logger.error("Encrypt input string is empty");
            return null;
        }

        addProvider();

        byte[] message = str.getBytes(StandardCharsets.UTF_8);
        try {
            MessageDigest md = MessageDigest.getInstance("SM3");
            byte[] digest = md.digest(message);
            return bytesToHex(digest);
        } catch (Exception e) {
            logger.error("Error in encrypting string with SM3 algorithm");
            logger.error(e.toString());
            return null;
        }
    }

}
