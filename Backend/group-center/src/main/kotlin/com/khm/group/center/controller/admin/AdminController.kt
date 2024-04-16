package com.khm.group.center.controller.admin

import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class AdminController {

    @Operation(summary = "Test")
    @RequestMapping("/admin/test", method = [RequestMethod.GET])
    fun test(): String {
        return "Ok"
    }

}
