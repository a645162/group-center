package com.khm.group.center.datatype.config.feature

open class BaseFeatureConfig {
    var enable = false

    open fun isValid(): Boolean {
        return enable
    }
}
