package com.khm.group.center.message

data class MessageItem(
    val content: String,
    val targetUser: String,
    val sendTime: Int?
) {
    override fun toString(): String {
        return "[$targetUser]$content"
    }
}
