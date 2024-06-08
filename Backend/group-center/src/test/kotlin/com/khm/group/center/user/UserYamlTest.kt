package com.khm.group.center.user

import com.khm.group.center.config.feature.GroupUserConfigParser
import org.junit.jupiter.api.Test

import com.khm.group.center.utils.file.ProgramFile

class UserYamlTest {
    @Test
    fun testReadUserYaml() {
//        val path = "./Config/Users/Master/2023.yaml"
        val path = "./Config/Users/PhD/phd.yaml"

        val text = ProgramFile.readFile(path).trim()

        assert(text.isNotEmpty())

        val result = GroupUserConfigParser.parseUserYaml(text)
        println(result.size)
        for (user in result) {
            println(
                "Name:${user.name}\n" +
                        "\tEng:${user.nameEng}\n" +
                        "\tWeComId:${user.weCom.userId}"
            )
        }
    }

    @Test
    fun testReadUserInDir() {
        val path = "./Config/Users"

        val result = GroupUserConfigParser.parseUserYamlInDir(path)

        assert(result.isNotEmpty())

        println(result.size)
        for (user in result) {
            println(
                "Name:${user.name}\n" +
                        "\tEng:${user.nameEng}\n" +
                        "\tWeComId:${user.weCom.userId}"
            )
        }
    }
}
