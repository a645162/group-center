package com.khm.group.center.db.model.subscription

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.time.LocalDateTime

/**
 * 项目订阅数据库模型
 */
@TableName(value = "project_subscription")
class ProjectSubscriptionModel {

    @TableId(type = IdType.AUTO)
    var id: Long? = null

    /**
     * 项目ID
     */
    var projectId: String = ""

    /**
     * 用户英文名
     */
    var userNameEng: String = ""

    /**
     * 用户中文名
     */
    var userName: String = ""

    /**
     * 订阅状态: pending, completed
     */
    var status: String = "pending"

    /**
     * 创建时间
     */
    var createdTime: LocalDateTime? = null

    /**
     * 完成时间
     */
    var completedTime: LocalDateTime? = null

    /**
     * 关联的任务ID
     */
    var taskId: String? = null

    companion object {
        /**
         * 创建新的待处理订阅
         */
        fun createPending(projectId: String, userNameEng: String, userName: String): ProjectSubscriptionModel {
            return ProjectSubscriptionModel().apply {
                this.projectId = projectId
                this.userNameEng = userNameEng
                this.userName = userName
                this.status = "pending"
                this.createdTime = LocalDateTime.now()
            }
        }

        /**
         * 创建新的待处理订阅（带任务ID）
         */
        fun createPendingWithTaskId(projectId: String, userNameEng: String, userName: String, taskId: String): ProjectSubscriptionModel {
            return ProjectSubscriptionModel().apply {
                this.projectId = projectId
                this.userNameEng = userNameEng
                this.userName = userName
                this.status = "pending"
                this.taskId = taskId
                this.createdTime = LocalDateTime.now()
            }
        }
    }

    /**
     * 标记为已完成
     */
    fun markAsCompleted() {
        this.status = "completed"
        this.completedTime = LocalDateTime.now()
    }

    /**
     * 检查是否已完成
     */
    fun isCompleted(): Boolean {
        return status == "completed"
    }

    /**
     * 检查是否待处理
     */
    fun isPending(): Boolean {
        return status == "pending"
    }
}