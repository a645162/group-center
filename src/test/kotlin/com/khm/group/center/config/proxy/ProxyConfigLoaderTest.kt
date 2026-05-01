package com.khm.group.center.config.proxy

import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.khm.group.center.datatype.config.ProxyConfig
import com.khm.group.center.datatype.config.ProxyConfigManager
import com.khm.group.center.datatype.config.ProxyStatus
import com.khm.group.center.utils.time.DateTimeUtils
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class ProxyConfigLoaderTest {

    @BeforeEach
    fun setup() {
        ProxyConfigManager.proxyConfig = ProxyConfig()
        ProxyConfigManager.proxyStatusMap.clear()
    }

    @Test
    fun testProxyConfigFileExists() {
        val configFilePath = java.nio.file.Paths.get("Config/Proxy/proxy.yaml")
        assertTrue(java.nio.file.Files.exists(configFilePath)) {
            "Proxy config file should exist at: $configFilePath (current dir: ${System.getProperty("user.dir")})"
        }
    }

    @Test
    fun testProxyConfigCanBeParsed() {
        val configFilePath = java.nio.file.Paths.get("Config/Proxy/proxy.yaml")
        val yamlContent = java.nio.file.Files.readString(configFilePath)

        val yamlMapper = com.fasterxml.jackson.databind.ObjectMapper(
            com.fasterxml.jackson.dataformat.yaml.YAMLFactory()
        ).registerKotlinModule()

        val config = yamlMapper.readValue(yamlContent, ProxyConfig::class.java)

        assertTrue(config.enable) { "Proxy config should be enabled" }
        assertEquals(200, config.version) { "Config version should be 200" }
        assertTrue(config.proxyTestList.isNotEmpty()) { "Should have at least one proxy test server" }
    }

    @Test
    fun testProxyStatusTimestampSeconds() {
        val status = ProxyStatus()

        // updateSuccess should store seconds
        status.updateSuccess(100)
        val checkTimeAfterSuccess = status.lastCheckTime
        val successTime = status.lastSuccessTime

        assertNotNull(checkTimeAfterSuccess)
        assertNotNull(successTime)

        // Verify: seconds timestamp should be around current time in seconds
        val nowSeconds = System.currentTimeMillis() / 1000
        val diff = kotlin.math.abs(nowSeconds - checkTimeAfterSuccess!!)
        assertTrue(diff < 5) { "lastCheckTime should be in seconds, got $checkTimeAfterSuccess (current seconds: $nowSeconds)" }

        // Verify: can be correctly formatted with DateTimeUtils (which expects seconds)
        val formatted = DateTimeUtils.formatDateTimeFull(
            DateTimeUtils.convertTimestampToDateTime(checkTimeAfterSuccess)
        )
        val currentYear = LocalDateTime.now().year
        assertTrue(formatted.contains(currentYear.toString())) {
            "Formatted time should contain current year $currentYear, got: $formatted"
        }
    }

    @Test
    fun testProxyStatusFailureTimestampSeconds() {
        val status = ProxyStatus()
        status.updateSuccess(100)  // Set lastSuccessTime
        status.updateFailure("test error")

        val checkTimeAfterFailure = status.lastCheckTime
        assertNotNull(checkTimeAfterFailure)

        val nowSeconds = System.currentTimeMillis() / 1000
        val diff = kotlin.math.abs(nowSeconds - checkTimeAfterFailure!!)
        assertTrue(diff < 5) { "lastCheckTime after failure should be in seconds, got $checkTimeAfterFailure" }

        // offlineDurationMinutes should be 0 (just went offline)
        assertEquals(0L, status.offlineDurationMinutes)
    }

    @Test
    fun testProxyStatusAlarmTimestampSeconds() {
        val status = ProxyStatus()
        status.recordAlarmTime()

        val alarmTime = status.lastAlarmTime
        assertNotNull(alarmTime)

        val nowSeconds = System.currentTimeMillis() / 1000
        val diff = kotlin.math.abs(nowSeconds - alarmTime!!)
        assertTrue(diff < 5) { "lastAlarmTime should be in seconds, got $alarmTime" }
    }

    @Test
    fun testOfflineDurationCalculation() {
        val status = ProxyStatus()

        // Simulate: success 30 minutes ago
        val thirtyMinAgo = System.currentTimeMillis() / 1000 - 30 * 60
        status.lastSuccessTime = thirtyMinAgo
        status.lastCheckTime = thirtyMinAgo
        status.isAvailable = true

        // Now fail
        status.updateFailure("connection lost")

        assertTrue(status.offlineDurationMinutes >= 29) {
            "Offline duration should be ~30 minutes, got ${status.offlineDurationMinutes}"
        }
    }

    @Test
    fun testTimestampNotInYear58296() {
        val status = ProxyStatus()
        status.updateSuccess(100)

        val formatted = DateTimeUtils.formatDateTimeFull(
            DateTimeUtils.convertTimestampToDateTime(status.lastCheckTime!!)
        )

        // The bug was: timestamps in milliseconds passed to ofEpochSecond() produced year ~58296
        assertFalse(formatted.contains("58296")) {
            "Time should not be in year 58296 (millisecond/second mismatch bug), got: $formatted"
        }
        assertFalse(formatted.startsWith("+")) {
            "Time should not start with '+' (overflow indicator), got: $formatted"
        }
    }
}
