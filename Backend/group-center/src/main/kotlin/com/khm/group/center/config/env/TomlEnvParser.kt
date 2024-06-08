package com.khm.group.center.config.env

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.tree.nodes.TomlArrayOfTablesElement
import com.akuleshov7.ktoml.tree.nodes.TomlKeyValuePrimitive
import com.akuleshov7.ktoml.tree.nodes.TomlTable
import java.util.*
import kotlin.collections.HashMap

class TomlEnvParser {
    companion object {
        fun parseTomlText(
            text: String,
            includeClassName: Boolean = true
        ): HashMap<String, String> {
            val result: HashMap<String, String> = HashMap()

            // Parse Toml Text
            val tomlRoot = Toml().tomlParser.parseString(text)

            for (classChild in tomlRoot.children) {
                val classTomlTable = classChild as TomlTable

                var className =
                    if (includeClassName) {
                        classTomlTable.name
                    } else {
                        ""
                    }
                if (className.isNotEmpty()) className = className.plus("_")

                for (tomlArrayOfTablesElement in classTomlTable.children) {
                    for (tomlKeyValueObj in (tomlArrayOfTablesElement as TomlArrayOfTablesElement).children) {
                        val tomlKeyValue = tomlKeyValueObj as TomlKeyValuePrimitive

                        val key =
                            (className + tomlKeyValue.key.toString())
                                .uppercase(Locale.getDefault())
                        val value = tomlKeyValue.value.content.toString().trim()

                        if (value.isEmpty()) {
                            continue
                        }

                        result[key] = value
                    }

                }
            }

            return result
        }
    }
}
