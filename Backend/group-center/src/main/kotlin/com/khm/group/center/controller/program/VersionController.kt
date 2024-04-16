package com.khm.group.center.controller.program

import com.khm.group.center.utils.program.ProgramInfo
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class VersionController {

    @Operation(summary = "GET 程序版本")
    @RequestMapping("/version", method = [RequestMethod.GET])
    fun version(): String {
        val version = ProgramInfo.getVersion()
        return version
    }

}
