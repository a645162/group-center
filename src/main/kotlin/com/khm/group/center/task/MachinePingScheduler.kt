package com.khm.group.center.task

import com.khm.group.center.datatype.config.MachineConfig
import com.khm.group.center.service.MachineStatusService
import com.khm.group.center.utils.program.Slf4jKt
import com.khm.group.center.utils.program.Slf4jKt.Companion.logger
import kotlinx.coroutines.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * 机器ping定时任务
 * 定期对机器进行ping检测，更新机器状态
 */
@Component
@Slf4jKt
class MachinePingScheduler {

    @Autowired
    lateinit var machineStatusService: MachineStatusService

    private var isInitialized = false

    /**
     * 每10分钟执行一次ping检测
     */
    @Scheduled(fixedRate = 600000) // 10分钟 = 600000毫秒
    fun scheduledPing() {
        if (MachineConfig.machineList.isEmpty()) {
            logger.warn("Machine list is empty, skip ping check")
            return
        }

        // 使用协程并发执行ping检测
        runBlocking {
            val pingJobs = MachineConfig.machineList.map { machine ->
                async(Dispatchers.IO) {
                    try {
                        machineStatusService.pingMachine(machine)
                    } catch (e: Exception) {
                        logger.debug("Ping machine ${machine.nameEng} exception: ${e.message}")
                        false
                    }
                }
            }

            // 等待所有ping任务完成
            val results = pingJobs.awaitAll()
            val successCount = results.count { it }
            val failedCount = results.size - successCount

            if (failedCount > 0) {
                logger.debug("Machine ping check completed: $successCount successful, $failedCount failed")
            }
        }
    }

    /**
     * 每10分钟执行一次状态清理（清理过期状态）
     */
    @Scheduled(fixedRate = 600000) // 10分钟 = 600000毫秒
    fun scheduledCleanup() {
        machineStatusService.cleanupExpiredStatus()
        logger.debug("Machine status cleanup completed")
    }

    /**
     * 应用启动时初始化机器状态（只执行一次）
     */
    @Scheduled(fixedDelay = 5000, initialDelay = 10000) // 启动后10秒执行，然后每5秒检查一次
    fun initializeOnStartup() {
        if (isInitialized) {
            return
        }
        
        if (MachineConfig.machineList.isNotEmpty()) {
            machineStatusService.initializeMachineStatus()
            logger.info("Machine status service initialization completed")
            isInitialized = true
        }
    }
}