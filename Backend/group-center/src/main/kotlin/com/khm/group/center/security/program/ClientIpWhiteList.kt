package com.khm.group.center.security.program

class ClientIpWhiteList {
    companion object {
        val ipWhiteList: HashMap<String, String> = HashMap()

        fun addIpToWhiteList(ipAddress: String) {
            // 添加 IP 地址到白名单
            ipWhiteList[ipAddress] = "white"
        }

        fun checkIpIsInWhiteList(ipAddress: String): Boolean {
            return ipWhiteList.containsKey(ipAddress)
        }
    }
}