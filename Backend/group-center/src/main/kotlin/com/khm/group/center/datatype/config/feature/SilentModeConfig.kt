package com.khm.group.center.datatype.config.feature

class SilentModeConfig : BaseFeatureConfig() {
    var startTime: String = ""
    var endTime: String = ""

    fun parse() {

    }

    override fun isValid(): Boolean {
        return super.isValid() && startTime.isNotEmpty() && endTime.isNotEmpty()
    }
}
