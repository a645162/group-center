package com.khm.group.center.datatype.response

data class FrontEndMachine(
    val machineName: String,
    val machineUrl: String,
    val urlKeywords: List<String>,
    val position: String,
)
