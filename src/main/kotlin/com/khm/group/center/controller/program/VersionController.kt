package com.khm.group.center.controller.program

import com.khm.group.center.utils.program.ProgramInfo
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "Program Info", description = "Program information and version API")
class VersionController {

    @Operation(
        summary = "Get Program Version",
        description = "Retrieve the current version of the application"
    )
    @RequestMapping("/version", method = [RequestMethod.GET])
    fun version(): String {
        val version = ProgramInfo.getVersion()
        return version
    }

}
