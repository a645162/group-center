package com.khm.group.center.controller.api.client

import com.khm.group.center.datatype.response.ClientResponse
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class ClientController {
    @Operation(summary = "Test Client API")
    @RequestMapping("/api/client/test", method = [RequestMethod.GET])
    fun test(): ClientResponse {
        val responseObj = ClientResponse()
        responseObj.result = "success"
        responseObj.isAuthenticated = true
        return responseObj
    }
}
