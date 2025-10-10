package com.khm.group.center.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * 心跳配置类
 * 支持通过application.yml或环境变量配置心跳相关参数
 */
@Configuration
@ConfigurationProperties(prefix = "heartbeat")
class HeartbeatConfig {

    /**
     * 时间同步阈值（秒）
     * 当客户端时间与服务器时间差超过此值时，触发时间同步报警
     * 默认：120秒（2分钟）
     */
    var timeSyncThreshold: Int = 120

    /**
     * 离线超时时间（秒）
     * 当机器超过此时间无心跳时，标记为离线
     * 默认：7200秒（2小时）
     */
    var offlineTimeout: Int = 7200

    /**
     * 在线状态检查时间（秒）
     * 检查机器是否在线的时间阈值
     * 默认：120秒（2分钟）
     */
    var onlineCheckInterval: Int = 120

    /**
     * 定时任务执行间隔（毫秒）
     * ping检测和状态清理的执行频率
     * 默认：300000毫秒（5分钟）
     */
    var scheduledInterval: Long = 300000

    override fun toString(): String {
        return "HeartbeatConfig(" +
                "timeSyncThreshold=$timeSyncThreshold, " +
                "offlineTimeout=$offlineTimeout, " +
                "onlineCheckInterval=$onlineCheckInterval, " +
                "scheduledInterval=$scheduledInterval" +
                ")"
    }
}