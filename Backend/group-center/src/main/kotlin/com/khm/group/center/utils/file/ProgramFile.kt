package com.khm.group.center.utils.file

import org.mozilla.universalchardet.UniversalDetector
import java.io.File

class ProgramFile {

    companion object {

        fun readFile(filePath: String): String {
            // Read File
            val file = File(filePath)

            return file.readText(Charsets.UTF_8)
        }

        fun readFileWithEncodingPredict(filePath: String): String {
            val file = File(filePath)

            val encoding =
                UniversalDetector.detectCharset(file) ?: return file.readText(Charsets.UTF_8)

            if (encoding == "UTF-8") {
                return file.readText(Charsets.UTF_8)
            } else {
                return file.readText(Charsets.US_ASCII)
            }
        }

    }

}