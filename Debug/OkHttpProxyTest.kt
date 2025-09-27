package org.example

import java.net.InetSocketAddress
import java.net.Proxy
import okhttp3.OkHttpClient
import okhttp3.Request

fun main() {
    println("---通过代理访问YouTube---")
    testYouTubeProxy()
    println("\n---不通过代理访问YouTube---")
    testYouTubeDirect()
}

fun testYouTubeProxy() {
    val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress("proxy.329509.xyz", 7890))
    val client = OkHttpClient.Builder()
        .proxy(proxy)
        .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .build()
    val request = Request.Builder()
        .url("https://www.youtube.com")
        .build()
    try {
        client.newCall(request).execute().use { response ->
            println("Response code: ${response.code}")
            println("Response body (first 200 chars): ${response.body?.string()?.take(200)}")
        }
    } catch (e: Exception) {
        println("Error: ${e.message}")
        e.printStackTrace()
    }
}

fun testYouTubeDirect() {
    val client = OkHttpClient.Builder()
        .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .build()
    val request = Request.Builder()
        .url("https://www.youtube.com")
        .build()
    try {
        client.newCall(request).execute().use { response ->
            println("Response code: ${response.code}")
            println("Response body (first 200 chars): ${response.body?.string()?.take(200)}")
        }
    } catch (e: Exception) {
        println("Error: ${e.message}")
        e.printStackTrace()
    }
}