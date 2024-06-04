package com.khm.group.center.controller.api.client

import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class DeviceMonitorController {

    @Operation(summary = "Test")
    @RequestMapping("/api/manage/test", method = [RequestMethod.GET])
    fun test(): String {
        return "Ok"
    }

}