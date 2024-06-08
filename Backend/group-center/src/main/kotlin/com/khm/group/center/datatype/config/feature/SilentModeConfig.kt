package com.khm.group.center.datatype.config.feature

import com.khm.group.center.datatype.utils.datetime.TimeHM

class SilentModeConfig : BaseFeatureConfig() {
    var startTime: String = ""
    var endTime: String = ""

    var startTimeObj: TimeHM = TimeHM()
    var endTimeObj: TimeHM = TimeHM()

    fun parse() {
        if (startTime.isEmpty() || endTime.isEmpty()) {
            this.enable = false
            return
        }
        try {
            startTimeObj = TimeHM.parseFromString(startTime)
            endTimeObj = TimeHM.parseFromString(endTime)
            this.enable = true
        } catch (e: Exception) {
            e.message
            this.enable = false
        }
    }

    fun isSilentMode(): Boolean {
        if (this.enable.not()) {
            return false
        }

        val now = TimeHM.getNow()
        return TimeHM.isTimeWithinRange(now, startTimeObj, endTimeObj)
    }

    override fun isValid(): Boolean {
        return super.isValid() && startTime.isNotEmpty() && endTime.isNotEmpty()
    }
}
