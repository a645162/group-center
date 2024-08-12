package com.khm.group.center.security.password

import java.math.BigInteger
import java.security.MessageDigest

class MD5 {
    companion object {
        fun getMd5Hash(input: String): String {
            val md = MessageDigest.getInstance("MD5")
            val digest = md.digest(input.toByteArray())
            val bigInt = BigInteger(1, digest)
            return bigInt.toString(16).padStart(32, '0')
        }
    }
}
