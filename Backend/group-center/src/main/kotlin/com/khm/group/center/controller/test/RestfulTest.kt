package com.khm.group.center.controller.test

import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class RestfulTest {

    @Operation(summary = "Get Test")
    @RequestMapping("/test/get", method = [RequestMethod.GET])
    fun getTest(): String {
        return "Get Test"
    }

    @Operation(summary = "Post Test")
    @RequestMapping("/test/post", method = [RequestMethod.POST])
    fun postTest(@RequestBody body: MyRequestBody): String {
        return "Post Test: ${body.property1}, ${body.property2}"
    }

    data class MyRequestBody(
        val property1: String,
        val property2: Int
    )
}