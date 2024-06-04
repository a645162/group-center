package com.khm.group.center.user

import com.khm.group.center.config.GroupUser
import org.junit.jupiter.api.Test

import com.khm.group.center.utils.file.ProgramFile

class UserYamlTest {
    @Test
    fun testReadUserYaml() {
//        val path = "./Users/Master/2023.yaml"
        val path = "./Users/PhD/phd.yaml"

        val text = ProgramFile.readFile(path)

        val result = GroupUser.parseUserYaml(text)
        println(result.size)
        for (user in result) {
            println(
                "Name:${user.name}\n" +
                        "\tEng:${user.nameEng}\n" +
                        "\tWeComId:${user.weComUser.userId}"
            )
        }
    }

    @Test
    fun testReadUserInDir() {
        val path = "./Users"

        val result = GroupUser.parseUserYamlInDir(path)

        println(result.size)
        for (user in result) {
            println(
                "Name:${user.name}\n" +
                        "\tEng:${user.nameEng}\n" +
                        "\tWeComId:${user.weComUser.userId}"
            )
        }
    }
}
