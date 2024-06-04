package com.khm.group.center.config

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.tree.nodes.TomlArrayOfTablesElement
import com.akuleshov7.ktoml.tree.nodes.TomlKeyValuePrimitive
import com.akuleshov7.ktoml.tree.nodes.TomlTable
import java.util.*
import kotlin.collections.HashMap

class TomlEnvParser {
    companion object {
        fun parseTomlText(text: String): HashMap<String, String> {
            val result: HashMap<String, String> = HashMap()

            // Parse Toml Text
            val tomlRoot = Toml().tomlParser.parseString(text)

            for (classChild in tomlRoot.children) {
                val classTomlTable = classChild as TomlTable

                var className = classTomlTable.name
                if (className.isNotEmpty()) className = className.plus("_")

                for (tomlArrayOfTablesElement in classTomlTable.children) {
                    for (tomlKeyValueObj in (tomlArrayOfTablesElement as TomlArrayOfTablesElement).children) {
                        val tomlKeyValue = tomlKeyValueObj as TomlKeyValuePrimitive

                        val key =
                            (className + tomlKeyValue.key.toString())
                                .uppercase(Locale.getDefault())
                        val value = tomlKeyValue.value.content.toString()

                        result[key] = value
                    }

                }
            }

            return result
        }
    }
}
