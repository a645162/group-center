package com.khm.group.center.config

import com.akuleshov7.ktoml.Toml
import org.junit.jupiter.api.Test
import org.mozilla.universalchardet.UniversalDetector
import java.io.File
import java.nio.file.Paths


class TomlTest {

    @Test
    fun readTomlFile() {
        // Get current Work Directory
        val currentDir = System.getProperty("user.dir")
        println("Current Directory:${currentDir}")

        val path = "./Debug/FileEnvExample.toml"
        println("Toml Path:${path}")

        // Check File Encoding
        val file = File(path)
        println("File Encoding:${file.readText(Charsets.UTF_8)}")

        println("File Exist:${file.exists()}")
        assert(file.exists())

        val encoding = UniversalDetector.detectCharset(file)
        if (encoding != null) {
            println("File Encoding:${encoding}")
        } else {
            println("File Encoding:Unknown")
        }

        assert(encoding != null)

        val text = file.readText(Charsets.UTF_8)

        parseTomlText(text)
    }

    fun parseTomlText(text: String) {
        // Parse Toml Text
        val toml = Toml().tomlParser.parseString(text)
        println("Toml:${toml}")
    }
}
