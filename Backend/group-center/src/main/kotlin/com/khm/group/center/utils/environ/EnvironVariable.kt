package com.khm.group.center.utils.environ

class EnvironVariable {

    companion object {
        fun getEnvironVariableString(
            key: String, defaultValue: String
        ): String {
            val value = System.getenv(key)
            return value ?: defaultValue
        }
    }

}
