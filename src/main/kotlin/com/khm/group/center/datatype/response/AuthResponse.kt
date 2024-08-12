package com.khm.group.center.datatype.response

import com.khm.group.center.config.env.ConfigEnvironment
import com.khm.group.center.utils.program.ProgramInfo

class AuthResponse {
    var isAuthenticated: Boolean = false
    var isSucceed: Boolean = false
    var haveError: Boolean = false
    var result: String = ""
    var accessKey: String = ""
    var ipAddress: String = ""
    var rememberAuthIp: Boolean = ConfigEnvironment.MACHINE_AUTH_REMEMBER_IP
    var serverVersion = ProgramInfo.getVersion()
}
