package com.khm.group.center.controller.api.web.front_end

import com.khm.group.center.datatype.config.MachineConfig
import com.khm.group.center.datatype.config.dashboard.DashBoardSiteConfig
import com.khm.group.center.datatype.response.FrontEndMachine
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "Frontend Public", description = "Public frontend API for machine list and site configuration")
class FrontEndPublicController {

    @Operation(
        summary = "Get Public Machine List for Web Frontend",
        description = "Retrieve list of all machines with public information for web frontend display"
    )
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

    @Operation(
        summary = "Get Dashboard Site Class List",
        description = "Retrieve list of dashboard site classes for frontend configuration"
    )
    @RequestMapping("/web/open/front_end/publicSiteClassList", method = [RequestMethod.GET])
    fun getPublicSiteClassList(): List<DashBoardSiteConfig.Companion.DataDashBoardSiteClass> {
        return DashBoardSiteConfig.siteClassList
    }

}
