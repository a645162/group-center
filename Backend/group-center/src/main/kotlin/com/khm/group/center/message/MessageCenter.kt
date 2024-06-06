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

    fun stopMonitoring() {
        job.cancel()
    }

    private suspend fun monitorMessageQueue() {
        coroutineScope {
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
    }

    private fun sendMessage(message: MessageItem) {
        if (!message.machineConfig.haveValidWebHookService()) {
            println("No any valid webhook server.")
            return
        }

        println("Sending message: $message")

        val msgSender = MessageSender(message)
        msgSender.sendMessage()
    }

    fun enqueueMessage(message: MessageItem) {
        messageQueue.add(message)
    }


    companion object {
        private val messageCenter = MessageCenter()

        fun startMessageCenter() {
            messageCenter.startMessageMonitoring()
        }

        fun addNewMessage(message: MessageItem) {
            messageCenter.enqueueMessage(message)
        }

        fun stopMessageCenter() {
            messageCenter.stopMonitoring()
        }
    }
}
