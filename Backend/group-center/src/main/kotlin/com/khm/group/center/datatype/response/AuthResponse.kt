package com.khm.group.center.datatype.response

import com.khm.group.center.utils.program.ProgramInfo

class AuthResponse {
    var isAuthenticated: Boolean = false
    var isSucceed: Boolean = false
    var haveError: Boolean = false
    var result: String = ""
    var accessKey: String = ""
    var serverVersion = ProgramInfo.getVersion()
}
