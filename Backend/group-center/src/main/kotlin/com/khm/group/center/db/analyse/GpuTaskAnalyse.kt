package com.khm.group.center.db.analyse

import com.khm.group.center.db.query.GpuTaskQuery
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class GpuTaskAnalyse {
    @Autowired
    lateinit var gpuTaskQuery: GpuTaskQuery

}
