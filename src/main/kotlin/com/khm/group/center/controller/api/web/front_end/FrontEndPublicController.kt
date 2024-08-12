package com.khm.group.center.controller.api.web.front_end

import com.khm.group.center.datatype.config.MachineConfig
import com.khm.group.center.datatype.config.dashboard.DashBoardSiteConfig
import com.khm.group.center.datatype.response.FrontEndMachine
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class FrontEndPublicController {

    @Operation(summary = "Web前端获取GPU列表")
    @RequestMapping("/web/open/front_end/publicMachineList", method = [RequestMethod.GET])
    fun getPublicMachineList(): List<FrontEndMachine> {
        val machineList = mutableListOf<FrontEndMachine>()

        for (machineConfig in MachineConfig.machineList) {
            machineList.add(
                machineConfig.toFrontEndMachine()
            )
        }

        return machineList
    }

    @Operation(summary = "Dashboard站点列表")
    @RequestMapping("/web/open/front_end/publicSiteClassList", method = [RequestMethod.GET])
    fun getPublicSiteClassList(): List<DashBoardSiteConfig.Companion.DataDashBoardSiteClass> {
        return DashBoardSiteConfig.siteClassList
    }

}
