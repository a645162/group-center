package com.khm.group.center.controller.api.client.file

class AuthorizedKeysFile(var authorizedKeys: String) {

    data class AuthorizedKey(
        val key: String,
        var comment: String = "",
        var title: String = ""
    )

    private val authorizedKeysList = ArrayList<AuthorizedKey>()

    init {
        parse()
    }

    fun add(authorizedKey: AuthorizedKey) {
        for (currentObjIndex in authorizedKeysList.indices) {
            if (authorizedKeysList[currentObjIndex].key == authorizedKey.key) {
                if (authorizedKey.comment.isNotEmpty()) {
                    var comment = authorizedKeysList[currentObjIndex].comment.trim()

                    val oldCommentSpilt = comment.split("\n")
                    val newCommentSpilt = authorizedKey.comment.split("\n")

                    for (newCommentIndex in newCommentSpilt.indices) {
                        if (oldCommentSpilt.contains(newCommentSpilt[newCommentIndex])) {
                            continue
                        }
                        comment += "\n" + newCommentSpilt[newCommentIndex]
                    }

                    authorizedKeysList[currentObjIndex].comment = comment
                }

                return
            }
        }

        this.authorizedKeysList.add(authorizedKey)
    }

    fun parse() {
        // Remove empty lines
        val authorizedKeysStringList = authorizedKeys.split("\n")
        val authorizedKeysStringSet = HashSet<String>()
        for (authorizedKey in authorizedKeysStringList) {
            val authorizedKeyTrim = authorizedKey.trim()
            if (authorizedKeyTrim.isNotEmpty()) {
                authorizedKeysStringSet.add(authorizedKeyTrim)
            }
        }

        val stringList = authorizedKeysStringSet.toList()
        for (i in stringList.indices) {
            var publicKeyString = stringList[i]
            if (publicKeyString.startsWith("#")) {
                continue
            }

            val pubKeySpilt = publicKeyString.split(" ")

            var title = ""
            if (pubKeySpilt.size == 3) {
                publicKeyString = pubKeySpilt[0] + " " + pubKeySpilt[1]
                title = pubKeySpilt[2]
            }

            var comment = ""
            var commentStartIndex = i - 1
            while (commentStartIndex >= 0) {
                if (!stringList[commentStartIndex].startsWith("#")) {
                    break
                }
                comment = stringList[commentStartIndex] + "\n" + comment
                commentStartIndex--
            }
            comment = comment.trim()

            if (comment.isEmpty() && title.isNotEmpty()) {
                comment = "# $title"
            }

            authorizedKeysList.add(
                AuthorizedKey(publicKeyString, comment, title)
            )
        }

        println()
    }

    fun build(): String {
        val stringBuilder = StringBuilder()
        for (authorizedKey in authorizedKeysList) {
            if (authorizedKey.key.isEmpty()) {
                continue
            }

            if (authorizedKey.comment.isNotEmpty()) {
                stringBuilder.append(authorizedKey.comment)
                stringBuilder.append("\n")
            }

            stringBuilder.append(authorizedKey.key)
            if (authorizedKey.title.isNotEmpty()) {
                stringBuilder.append(" ")
                stringBuilder.append(authorizedKey.title)
            }
            stringBuilder.append("\n")

            stringBuilder.append("\n")
        }
        return stringBuilder.toString().trim() + "\n"
    }

    fun combine(authorizedKeysFile: AuthorizedKeysFile) {
        for (authorizedKey in authorizedKeysFile.authorizedKeysList) {
            add(authorizedKey)
        }
    }
}
// 转换为Python Class并且标注清楚类型，删除空行的时候不需要去重
