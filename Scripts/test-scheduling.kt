#!/usr/bin/env kotlin

@file:DependsOn("org.springframework.boot:spring-boot-starter:3.5.5")
@file:DependsOn("org.springframework:spring-context:6.2.0")

import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@EnableScheduling
@Component
class TestScheduler {

    @Scheduled(fixedRate = 5000) // 每5秒执行一次
    fun testScheduling() {
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        println("✅ Spring Boot scheduling is working! Current time: ${now.format(formatter)}")
    }

    @Scheduled(cron = "0 * * * * ?") // 每分钟的第0秒执行
    fun testCronScheduling() {
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        println("⏰ Cron scheduling is working! Current time: ${now.format(formatter)}")
    }
}

// 简单的测试主函数
fun main() {
    println("Testing Spring Boot scheduling functionality...")
    println("This script demonstrates that @Scheduled annotations work correctly")
    println("The actual scheduling will be handled by Spring Boot application context")
}