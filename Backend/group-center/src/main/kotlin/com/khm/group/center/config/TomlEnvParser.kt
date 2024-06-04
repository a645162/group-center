package com.khm.group.center.config

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.tree.nodes.TomlArrayOfTablesElement
import com.akuleshov7.ktoml.tree.nodes.TomlKeyValueArray
import com.akuleshov7.ktoml.tree.nodes.TomlKeyValuePrimitive
import com.akuleshov7.ktoml.tree.nodes.TomlTable

class TomlEnvParser {

    companion object {

        fun parseTomlText(text: String): HashMap<String, Any> {
            val result: HashMap<String, Any> = HashMap()

            // Parse Toml Text
            val tomlRoot = Toml().tomlParser.parseString(text)

            for (classChild in tomlRoot.children) {
                for (tomlArrayOfTablesElement in (classChild as TomlTable).children) {
                    for (tomlKeyValueObj in (tomlArrayOfTablesElement as TomlArrayOfTablesElement).children) {
                        val tomlKeyValue = tomlKeyValueObj as TomlKeyValuePrimitive

                        val key = tomlKeyValue.key.toString()
                        val value = tomlKeyValue.value.content

                        result[key] = value
                    }

                }
            }

            return result
        }

    }

}