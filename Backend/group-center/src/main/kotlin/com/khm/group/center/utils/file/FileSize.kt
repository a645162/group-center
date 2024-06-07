package com.khm.group.center.utils.file

class FileSize {
    companion object {
        fun fixText(originalText: String) {
            var newText = originalText

            while (newText.contains("GiB")) {
                newText = newText.replace("GiB", "GB")
            }

            while (newText.contains("MiB")) {
                newText = newText.replace("Mib", "MB")
            }
        }
    }
}
