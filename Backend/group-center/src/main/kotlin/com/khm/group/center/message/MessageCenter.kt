package com.khm.group.center.message

import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext

class MessageCenter : CoroutineScope {
    private val job = Job()
    private val messageQueue: Queue<MessageItem> = LinkedList()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    fun startMessageMonitoring() {
        launch {
            monitorMessageQueue()
        }
    }

    private suspend fun monitorMessageQueue() {
        while (isActive) { // 使用 isActive 来检查协程是否被取消
            if (messageQueue.isNotEmpty()) {
                val message = messageQueue.poll() ?: continue
                launch { // 启动一个新的协程来发送消息
                    sendMessage(message)
                }
            }
            delay(1000) // 每隔一秒检查一次队列
        }
    }

    private suspend fun sendMessage(message: MessageItem) {
        println("Sending message: ${message.toString()}")
        // 这里可以添加发送消息的实际逻辑，例如通过网络请求发送
        delay(1000) // 模拟发送消息的延迟
        println("Message sent: ${message.toString()}")
    }

    fun enqueueMessage(message: MessageItem) {
        messageQueue.add(message)
    }

    fun stopMonitoring() {
        job.cancel()
    }
}

fun main() = runBlocking<Unit> {
    val messageCenter = MessageCenter()

    // 启动队列监视
    messageCenter.startMessageMonitoring()

    // 添加一些消息到队列
    messageCenter.enqueueMessage(MessageItem("Hello", "User1", 12345))
    messageCenter.enqueueMessage(MessageItem("Hi", "User2", 12346))
    messageCenter.enqueueMessage(MessageItem("Hey", "User3", 12347))

    // 主线程等待一段时间，以便协程有足够的时间发送消息
    delay(5000)

    // 停止监视
    messageCenter.stopMonitoring()
}
