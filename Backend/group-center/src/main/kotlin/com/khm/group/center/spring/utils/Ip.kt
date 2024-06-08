package com.khm.group.center.spring.utils

import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes


class Ip {
    companion object {
        fun getControllerRemoteIp(): String {
            var request: HttpServletRequest? = null
            try {
                request =
                    (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request
            } catch (e: Exception) {
                println("Can not get current IP.")
            }
            return request!!.remoteAddr
        }
    }
}
