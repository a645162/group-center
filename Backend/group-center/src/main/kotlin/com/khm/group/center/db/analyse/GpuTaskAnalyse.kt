package com.khm.group.center.db.analyse

import com.khm.group.center.db.query.GpuTaskQuery
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class GpuTaskAnalyse {
    @Autowired
    lateinit var gpuTaskQuery: GpuTaskQuery

    /*
    * 机器视角
    * 机器使用时间占比
    *
    * */

    /*
    * 单个用户视角
    * 每个机器的使用时间占比(每个机器/总时间)
    * */

}