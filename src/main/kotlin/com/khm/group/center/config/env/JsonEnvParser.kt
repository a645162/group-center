package com.khm.group.center.config.env

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

class JsonEnvParser {
    companion object {
        private val objectMapper = ObjectMapper()
        
        fun parseJsonText(
            text: String,
            includeClassName: Boolean = true
        ): HashMap<String, String> {
            val result = HashMap<String, String>()

            val jsonRoot = objectMapper.readValue<Map<String, Any>>(text)
            for (originalClassName in jsonRoot.keys) {
                var className =
                    if (includeClassName) {
                        originalClassName
                    } else {
                        ""
                    }
                val classJson = objectMapper.readValue<Map<String, String>>(
                    objectMapper.writeValueAsString(jsonRoot[originalClassName])
                )

                if (className.isNotEmpty()) className = className.plus("_")

                for (originalKey in classJson.keys) {
                    val key = (className + originalKey).uppercase()
                    val value = classJson[originalKey]?.trim() ?: ""

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
