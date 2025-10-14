package com.khm.group.center.db.mapper.subscription

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.khm.group.center.db.model.subscription.ProjectSubscriptionModel
import org.apache.ibatis.annotations.Mapper

/**
 * 项目订阅Mapper接口
 */
@Mapper
interface ProjectSubscriptionMapper : BaseMapper<ProjectSubscriptionModel> {

    /**
     * 根据项目ID和状态查询订阅列表
     */
    fun findByProjectIdAndStatus(projectId: String, status: String): List<ProjectSubscriptionModel> {
        val queryWrapper = QueryWrapper<ProjectSubscriptionModel>()
            .eq("project_id", projectId)
            .eq("status", status)
        return selectList(queryWrapper)
    }

    /**
     * 根据用户英文名和状态查询订阅列表
     */
    fun findByUserNameEngAndStatus(userNameEng: String, status: String): List<ProjectSubscriptionModel> {
        val queryWrapper = QueryWrapper<ProjectSubscriptionModel>()
            .eq("user_name_eng", userNameEng)
            .eq("status", status)
        return selectList(queryWrapper)
    }

    /**
     * 根据用户英文名查询所有订阅
     */
    fun findByUserNameEng(userNameEng: String): List<ProjectSubscriptionModel> {
        val queryWrapper = QueryWrapper<ProjectSubscriptionModel>()
            .eq("user_name_eng", userNameEng)
        return selectList(queryWrapper)
    }

    /**
     * 根据项目ID查询所有订阅
     */
    fun findByProjectId(projectId: String): List<ProjectSubscriptionModel> {
        val queryWrapper = QueryWrapper<ProjectSubscriptionModel>()
            .eq("project_id", projectId)
        return selectList(queryWrapper)
    }

    /**
     * 查询所有待处理的订阅
     */
    fun findAllPending(): List<ProjectSubscriptionModel> {
        val queryWrapper = QueryWrapper<ProjectSubscriptionModel>()
            .eq("status", "pending")
        return selectList(queryWrapper)
    }

    /**
     * 根据项目ID和用户英文名查询订阅
     */
    fun findByProjectIdAndUserNameEng(projectId: String, userNameEng: String): ProjectSubscriptionModel? {
        val queryWrapper = QueryWrapper<ProjectSubscriptionModel>()
            .eq("project_id", projectId)
            .eq("user_name_eng", userNameEng)
        return selectOne(queryWrapper)
    }

    /**
     * 根据项目ID和用户英文名查询待处理的订阅
     */
    fun findPendingByProjectIdAndUserNameEng(projectId: String, userNameEng: String): ProjectSubscriptionModel? {
        val queryWrapper = QueryWrapper<ProjectSubscriptionModel>()
            .eq("project_id", projectId)
            .eq("user_name_eng", userNameEng)
            .eq("status", "pending")
        return selectOne(queryWrapper)
    }

    /**
     * 根据任务ID查询订阅
     */
    fun findByTaskId(taskId: String): ProjectSubscriptionModel? {
        val queryWrapper = QueryWrapper<ProjectSubscriptionModel>()
            .eq("task_id", taskId)
        return selectOne(queryWrapper)
    }

    /**
     * 根据任务ID查询待处理的订阅
     */
    fun findPendingByTaskId(taskId: String): ProjectSubscriptionModel? {
        val queryWrapper = QueryWrapper<ProjectSubscriptionModel>()
            .eq("task_id", taskId)
            .eq("status", "pending")
        return selectOne(queryWrapper)
    }

    /**
     * 检查用户是否已订阅项目
     */
    fun existsByProjectIdAndUserNameEng(projectId: String, userNameEng: String): Boolean {
        val queryWrapper = QueryWrapper<ProjectSubscriptionModel>()
            .eq("project_id", projectId)
            .eq("user_name_eng", userNameEng)
        return selectCount(queryWrapper) > 0
    }

    /**
     * 检查用户是否已订阅项目（待处理状态）
     */
    fun existsPendingByProjectIdAndUserNameEng(projectId: String, userNameEng: String): Boolean {
        val queryWrapper = QueryWrapper<ProjectSubscriptionModel>()
            .eq("project_id", projectId)
            .eq("user_name_eng", userNameEng)
            .eq("status", "pending")
        return selectCount(queryWrapper) > 0
    }

    /**
     * 删除用户的订阅
     */
    fun deleteByProjectIdAndUserNameEng(projectId: String, userNameEng: String): Int {
        val queryWrapper = QueryWrapper<ProjectSubscriptionModel>()
            .eq("project_id", projectId)
            .eq("user_name_eng", userNameEng)
        return delete(queryWrapper)
    }

    /**
     * 删除用户的待处理订阅
     */
    fun deletePendingByProjectIdAndUserNameEng(projectId: String, userNameEng: String): Int {
        val queryWrapper = QueryWrapper<ProjectSubscriptionModel>()
            .eq("project_id", projectId)
            .eq("user_name_eng", userNameEng)
            .eq("status", "pending")
        return delete(queryWrapper)
    }

    /**
     * 根据项目ID删除所有订阅
     */
    fun deleteByProjectId(projectId: String): Int {
        val queryWrapper = QueryWrapper<ProjectSubscriptionModel>()
            .eq("project_id", projectId)
        return delete(queryWrapper)
    }

    /**
     * 根据用户英文名删除所有订阅
     */
    fun deleteByUserNameEng(userNameEng: String): Int {
        val queryWrapper = QueryWrapper<ProjectSubscriptionModel>()
            .eq("user_name_eng", userNameEng)
        return delete(queryWrapper)
    }
}