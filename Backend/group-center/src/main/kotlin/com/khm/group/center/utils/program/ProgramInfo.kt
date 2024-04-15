package com.khm.group.center.utils.program

import java.io.IOException
import java.util.*

class ProgramInfo {

    companion object {
        fun getVersion(): String {
            val props = Properties()
            try {
                val inputStream =
                    this::class.java.classLoader.getResourceAsStream(
                        "settings/version.properties"
                    )
                props.load(inputStream)
            } catch (e: IOException) {
                println("Could not read version.properties file: ${e.message}")
                return ""
            }

            return props.getProperty("version")
        }
    }
}
