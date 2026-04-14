package com.khm.group.center.datatype.response

import com.fasterxml.jackson.annotation.JsonProperty
import com.khm.group.center.utils.program.ProgramInfo

class ClientResponse {
    var serverVersion = ProgramInfo.getVersion()

    @get:JsonProperty("isAuthenticated")
    @set:JsonProperty("isAuthenticated")
    var isAuthenticated: Boolean = true

    var haveError: Boolean = false

    @get:JsonProperty("isSucceed")
    @set:JsonProperty("isSucceed")
    var isSucceed: Boolean = true

    var result: Any? = null
}
