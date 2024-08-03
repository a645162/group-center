package com.khm.group.center.db.analyse

import com.khm.group.center.datatype.summary.GpuSummary
import com.khm.group.center.datatype.summary.PersonSummary
import com.khm.group.center.db.query.GpuTaskQuery
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class GpuTaskAnalyse {
    @Autowired
    lateinit var gpuTaskQuery: GpuTaskQuery

    // Gpu列表
    val gpuList = mutableListOf<GpuSummary>()

    // Gpu型号列表
    val gpuModelList = mutableListOf<GpuSummary>()

    // 用户列表
    val userList = mutableListOf<PersonSummary>()

    // 熬夜冠军(最晚睡的人，截止到凌晨4点)
    var dailySleepLateChampion: PersonSummary? = null
}
