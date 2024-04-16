package com.khm.group.center.security;

import org.junit.jupiter.api.Test;

public class SM3Test {

    @Test
    public void TestSm3() {
        String str = "https://github.com/a645162/group-center";

        // Hex Version
        String cryptoResult =
                ProgramCrypto.hashSM3HexStr(str);
        if (cryptoResult != null) {
            System.out.println("Output Hex: " + cryptoResult);
            System.out.println("Length: " + cryptoResult.length());
        }
        assert cryptoResult != null;
        assert cryptoResult.length() == 64;

        // Base64 Version
        cryptoResult =
                ProgramCrypto.hashSM3Base64Str(str);
        if (cryptoResult != null) {
            System.out.println("Output Base64: " + cryptoResult);
            System.out.println("Length: " + cryptoResult.length());
        }
        assert cryptoResult != null;
    }

    @Test
    public void TestJWTSm3() {
        String str = "https://github.com/a645162/group-center";
        String key = "a645162/group-center";

        assert ProgramCrypto.encryptHmacSM3HexStr(str, key) != null;
        assert ProgramCrypto.encryptHmacSM3Base64Str(str, key) != null;

        String encryptResult =
                ProgramCrypto.encryptJWTString(str, key);

        String decryptResult =
                ProgramCrypto.decryptJWTString(encryptResult, key);

        System.out.println("Original: " + str);
        System.out.println("Encrypt: " + encryptResult);
        System.out.println("Decrypt: " + decryptResult);

        assert str.equals(decryptResult);
    }

}
