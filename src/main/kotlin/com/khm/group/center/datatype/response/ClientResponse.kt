package com.khm.group.center.datatype.response

import com.khm.group.center.utils.program.ProgramInfo

class ClientResponse {
    var serverVersion = ProgramInfo.getVersion()
    var isAuthenticated: Boolean = true
    var haveError: Boolean = false
    var isSucceed: Boolean = true
    var result: String = ""
}
