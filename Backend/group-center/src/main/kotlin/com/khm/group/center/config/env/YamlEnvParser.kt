package com.khm.group.center.config.env

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlScalar
import java.util.*
import kotlin.collections.HashMap

class YamlEnvParser {
    companion object {
        fun parseYamlText(
            text: String,
            includeClassName: Boolean = true
        ): HashMap<String, String> {
            val result = HashMap<String, String>()

            val yamlRoot = Yaml.default.parseToYamlNode(text) as YamlMap
            for (yamlNode1 in yamlRoot.entries) {
                var className =
                    if (includeClassName) {
                        yamlNode1.key.content.trim()
                    } else {
                        ""
                    }
                if (className.isNotEmpty()) className += "_"

                val classNode = yamlNode1.value as YamlMap

                for (yamlNode2 in classNode.entries) {
                    val key = (className + yamlNode2.key.content).uppercase(Locale.getDefault())
                    val value = (yamlNode2.value as YamlScalar).content.trim()

                    if (value.isEmpty()) {
                        continue
                    }

                    result[key] = value
                }
            }

            return result
        }
    }
}
