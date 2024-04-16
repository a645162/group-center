package com.khm.group.center.security;

import org.junit.jupiter.api.Test;

import static com.khm.group.center.security.ProgramCrypto.encryptString;

public class SM3Test {

    @Test
    public void TestSm3() {
        String str = "https://github.com/a645162/group-center";

        String cryptoResult = encryptString(str);

        if (cryptoResult != null) {
            System.out.println("Output Hex: " + cryptoResult);
            System.out.println("Length: " + cryptoResult.length());
        }

        assert cryptoResult != null;
        assert cryptoResult.length() == 64;
    }

}
