package com.khm.group.center.controller.api.web.public

import io.swagger.v3.oas.annotations.Operation
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class IpController {
    @Operation(summary = "获取用户的IP")
    @RequestMapping("/web/open/ip", method = [RequestMethod.GET])
    fun getUserIp(request: HttpServletRequest): String {
        // Try to get the IP address from the X-Forwarded-For header, which is set by proxy servers
        val forwardedFor = request.getHeader("X-Forwarded-For")
        if (forwardedFor != null && forwardedFor.isNotEmpty()) {
            // X-Forwarded-For may contain multiple IP addresses, take the first one as the real IP address of the client
            val ips = forwardedFor.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            return ips[0].trim()
        }

        // If X-Forwarded-For is not available, try to get the real IP from X-Real-IP header
        val realIp = request.getHeader("X-Real-IP")
        if (realIp != null && realIp.isNotEmpty()) {
            return realIp.trim()
        }

        // If no other headers are available, fall back to the remote address
        return request.remoteAddr
    }

}
