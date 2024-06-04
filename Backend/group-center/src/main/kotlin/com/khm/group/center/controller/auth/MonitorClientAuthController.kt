package com.khm.group.center.controller.auth

import com.khm.group.center.utils.program.ProgramInfo
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class MonitorClientAuthController {

    @Operation(summary = "认证GPU监控客户端")
    @RequestMapping("/gpu/client/auth", method = [RequestMethod.POST])
    fun auth(): String {
        val version = ProgramInfo.getVersion()
        return version
    }

}