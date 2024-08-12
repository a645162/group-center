package com.khm.group.center.config

import com.charleskorn.kaml.Yaml
import org.junit.jupiter.api.Test
import java.io.File

class YamlTest {
    @Test
    fun readYamlFile() {
        val filePath = "./Debug/FileEnvExample.yaml"
        val file = File(filePath)

        val text = file.readText(Charsets.UTF_8)

        parseYamlText(text)
    }

    fun parseYamlText(text: String) {
        val yamlRoot = Yaml.default.parseToYamlNode(text)

        println()
    }
}