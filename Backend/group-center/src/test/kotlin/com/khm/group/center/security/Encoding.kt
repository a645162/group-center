package com.khm.group.center.security

import com.khm.group.center.security.password.MD5
import org.junit.jupiter.api.Test

class Encoding {
    @Test
    fun testMd5() {
        val input = "test"
        val md5 = MD5.getMd5Hash(input)
        println(md5)
    }
}
