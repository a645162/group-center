package com.khm.group.center.user

import com.khm.group.center.config.feature.MachineConfigParser
import org.junit.jupiter.api.Test

import com.khm.group.center.utils.file.ProgramFile

class MachineYamlTest {
    @Test
    fun testReadMachineYaml() {
        val path = "./Config/Machine/gpu.yaml"

        val text = ProgramFile.readFile(path)

        val result = MachineConfigParser.parseMachineYaml(text)

        println(result.size)
    }

    @Test
    fun testReadMachineInDir() {
        val path = "./Config/Machine/Deploy"

        val result = MachineConfigParser.parseMachineYamlInDir(path)

        println(result.size)
        for (user in result) {
            println(
                "Name:${user.name}\n" +
                        "\tEng:${user.nameEng}\n" +
                        "\tWeComId:${user.weComServer.groupBotKey}"
            )
        }
    }
}
