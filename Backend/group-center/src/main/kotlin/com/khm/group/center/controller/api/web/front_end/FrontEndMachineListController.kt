package com.khm.group.center.controller.api.web.front_end

import com.khm.group.center.datatype.config.MachineConfig
import com.khm.group.center.datatype.response.FrontEndMachine
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class FrontEndMachineListController {

    @RequestMapping("/web/open/front_end/test", method = [RequestMethod.GET])
    fun test(): String {
        return "ok"
    }

    @RequestMapping("/web/open/front_end/machineList", method = [RequestMethod.GET])
    fun getMachineList(): List<FrontEndMachine> {
        val machineList = mutableListOf<FrontEndMachine>()

        for (machineConfig in MachineConfig.machineList) {
            machineList.add(
                machineConfig.toFrontEndMachine()
            )
        }

        return machineList
    }

}
