package com.khm.group.center.config.env

import com.alibaba.fastjson2.JSON

class JsonEnvParser {
    companion object {
        fun parseJsonText(
            text: String,
            includeClassName: Boolean = true
        ): HashMap<String, String> {
            val result = HashMap<String, String>()

            val jsonRoot = JSON.parseObject(text)
            for (originalClassName in jsonRoot.keys) {
                var className =
                    if (includeClassName) {
                        originalClassName
                    } else {
                        ""
                    }
                val classJson = jsonRoot.getJSONObject(originalClassName)

                if (className.isNotEmpty()) className = className.plus("_")

                for (originalKey in classJson.keys) {
                    val key = (className + originalKey).uppercase()
                    val value = classJson.getString(originalKey).trim()

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
