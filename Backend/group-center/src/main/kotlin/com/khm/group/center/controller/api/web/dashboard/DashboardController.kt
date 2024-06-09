package com.khm.group.center.controller.api.web.dashboard

import com.khm.group.center.datatype.response.ClientResponse
import com.khm.group.center.db.mapper.client.GpuTaskInfoMapper
import io.swagger.v3.oas.annotations.Operation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class DashboardController {

    @Autowired
    lateinit var gpuTaskInfoMapper: GpuTaskInfoMapper

    @Operation(summary = "更新面板")
    @RequestMapping("/web/dashboard/usage/update", method = [RequestMethod.GET])
    fun gpuTaskInfo(): ClientResponse {

        val result = ClientResponse()
        result.result = "success"
        return result
    }

}
