package com.khm.group.center.controller.api.client

import com.khm.group.center.utils.program.ProgramInfo
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class GpuMonitorController {

    @Operation(summary = "监控启动")
    @RequestMapping("/api/client/start", method = [RequestMethod.POST])
    fun version(): String {
        val version = ProgramInfo.getVersion()
        return version
    }

}