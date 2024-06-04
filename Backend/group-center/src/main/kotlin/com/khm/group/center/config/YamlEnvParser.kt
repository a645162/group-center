package com.khm.group.center.config

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlScalar
import java.util.*
import kotlin.collections.HashMap

class YamlEnvParser {

    companion object {

        fun parseYamlText(text: String): HashMap<String, String> {
            val result = HashMap<String, String>()

            val yamlRoot = Yaml.default.parseToYamlNode(text) as YamlMap
            for (yamlNode1 in yamlRoot.entries) {
                var className = yamlNode1.key.content.trim()
                if (className.isNotEmpty()) className += "_"

                val classNode = yamlNode1.value as YamlMap

                for (yamlNode2 in classNode.entries) {
                    val key = (className + yamlNode2.key.content).uppercase(Locale.getDefault())
                    val value = (yamlNode2.value as YamlScalar).content

                    result[key] = value
                }
            }

            return result
        }

    }

}
