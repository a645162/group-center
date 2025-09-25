package com.khm.group.center.service

import com.khm.group.center.db.model.client.GpuTaskInfoModel
import com.khm.group.center.utils.time.TimeAnalysisUtils
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.time.ZoneId

@SpringBootTest
class SleepAnalysisTest {

    @Autowired
    private lateinit var timeAnalysisUtils: TimeAnalysisUtils

    /**
     * 测试作息时间判断逻辑
     */
    @Test
    fun testSleepTimeLogic() {
        // 测试熬夜时间（00:00-04:00）
        val lateNightTime = LocalDateTime.of(2025, 1, 1, 2, 30) // 凌晨2:30
        val lateNightTimestamp = lateNightTime.atZone(ZoneId.systemDefault()).toEpochSecond()
        
        assert(timeAnalysisUtils.isLateNight(lateNightTimestamp)) { "凌晨2:30应该被识别为熬夜时间" }
        
        // 测试早起时间（04:00-10:00）
        val earlyMorningTime = LocalDateTime.of(2025, 1, 1, 6, 0) // 早上6:00
        val earlyMorningTimestamp = earlyMorningTime.atZone(ZoneId.systemDefault()).toEpochSecond()
        
        assert(timeAnalysisUtils.isEarlyMorning(earlyMorningTimestamp)) { "早上6:00应该被识别为早起时间" }
        
        // 测试正常时间（10:00-24:00）
        val normalTime = LocalDateTime.of(2025, 1, 1, 14, 0) // 下午2:00
        val normalTimestamp = normalTime.atZone(ZoneId.systemDefault()).toEpochSecond()
        
        assert(timeAnalysisUtils.isNormalTime(normalTimestamp)) { "下午2:00应该被识别为正常时间" }
        
        // 测试边界情况：凌晨4点整（应该属于早起，不属于熬夜）
        val boundaryTime = LocalDateTime.of(2025, 1, 1, 4, 0) // 凌晨4:00整
        val boundaryTimestamp = boundaryTime.atZone(ZoneId.systemDefault()).toEpochSecond()
        
        assert(!timeAnalysisUtils.isLateNight(boundaryTimestamp)) { "凌晨4:00整不应该被识别为熬夜时间" }
        assert(timeAnalysisUtils.isEarlyMorning(boundaryTimestamp)) { "凌晨4:00整应该被识别为早起时间" }
        
        println("✅ 作息时间判断逻辑测试通过")
    }

    /**
     * 测试作息分析功能
     */
    @Test
    fun testSleepAnalysis() {
        // 创建测试任务
        val tasks = listOf(
            createTestTask("user1", LocalDateTime.of(2025, 1, 1, 2, 30)), // 熬夜任务
            createTestTask("user2", LocalDateTime.of(2025, 1, 1, 6, 0)),  // 早起任务
            createTestTask("user3", LocalDateTime.of(2025, 1, 1, 14, 0)), // 正常时间任务
            createTestTask("user1", LocalDateTime.of(2025, 1, 1, 3, 0)),  // 另一个熬夜任务
            createTestTask("user2", LocalDateTime.of(2025, 1, 1, 4, 30))  // 另一个早起任务
        )

        // 设置时间范围（包含所有任务）
        val startTime = LocalDateTime.of(2025, 1, 1, 0, 0).atZone(ZoneId.systemDefault()).toEpochSecond()
        val endTime = LocalDateTime.of(2025, 1, 1, 23, 59).atZone(ZoneId.systemDefault()).toEpochSecond()

        // 进行作息分析
        val sleepAnalysis = timeAnalysisUtils.analyzeSleepPattern(tasks, startTime, endTime)

        // 验证结果
        assert(sleepAnalysis.totalLateNightTasks == 2) { "应该有2个熬夜任务" }
        assert(sleepAnalysis.totalEarlyMorningTasks == 2) { "应该有2个早起任务" }
        assert(sleepAnalysis.totalLateNightUsers == 1) { "应该有1个熬夜用户" }
        assert(sleepAnalysis.totalEarlyMorningUsers == 1) { "应该有1个早起用户" }
        
        // 验证熬夜冠军（最晚启动的任务）
        assert(sleepAnalysis.lateNightChampion != null) { "应该有熬夜冠军" }
        assert(sleepAnalysis.lateNightChampion!!.taskUser == "user1") { "熬夜冠军应该是user1" }
        
        // 验证早起冠军（最早启动的任务）
        assert(sleepAnalysis.earlyMorningChampion != null) { "应该有早起冠军" }
        assert(sleepAnalysis.earlyMorningChampion!!.taskUser == "user2") { "早起冠军应该是user2" }
        
        println("✅ 作息分析功能测试通过")
    }

    /**
     * 创建测试任务
     */
    private fun createTestTask(user: String, startTime: LocalDateTime): GpuTaskInfoModel {
        return GpuTaskInfoModel().apply {
            taskUser = user
            taskStartTime = startTime.atZone(ZoneId.systemDefault()).toEpochSecond()
            taskFinishTime = startTime.plusHours(1).atZone(ZoneId.systemDefault()).toEpochSecond()
            taskRunningTimeInSeconds = 3600
            taskStatus = "success"
            taskGpuName = "test-gpu"
            serverName = "test-server"
            projectName = "test-project"
            gpuUsagePercent = 80f
            gpuMemoryPercent = 50f
            taskGpuMemoryGb = 8.0f
            multiDeviceWorldSize = 1
            multiDeviceLocalRank = 0
        }
    }
}