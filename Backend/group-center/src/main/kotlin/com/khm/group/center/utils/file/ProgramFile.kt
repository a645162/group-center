package com.khm.group.center.utils.file

import org.mozilla.universalchardet.UniversalDetector
import java.io.File

class ProgramFile {

    companion object {

        // Auto recursive
        fun walkFileTreeKtRecursive(
            directoryPath: String,
            ext: String = "",
        ): List<String> {
            val files = mutableListOf<String>()

            File(directoryPath).walkTopDown().filter { it.isFile }
                .forEach { file ->
                    if (ext.isEmpty() || file.name.endsWith(ext, true)) {
                        files.add(file.absolutePath)
                    }
                }

            return files
        }

        fun walkFileTree(
            directoryPath: String,
            ext: String = "",
            recursive: Boolean = false
        ): List<String> {
            val files = mutableListOf<String>()
            val maxDepth = if (recursive) Int.MAX_VALUE else 1

            File(directoryPath).walk()
                .maxDepth(maxDepth)
                .filter { file ->
                    file.isFile && (ext.isEmpty() || file.extension.equals(ext, ignoreCase = true))
                }
                .forEach { file ->
                    files.add(file.path)
                }

            return files
        }

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
