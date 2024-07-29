package com.khm.group.center.interceptor

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONObject
import com.khm.group.center.config.env.ConfigEnvironment
import com.khm.group.center.datatype.response.AuthResponse
import com.khm.group.center.security.program.ClientAccessKey
import com.khm.group.center.security.program.ClientIpWhiteList
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView

@Component
class ClientAuthInterceptor : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val ipAddress = request.remoteAddr
        // println("Auth Interceptor Client IP: $ipAddress")

        if (ConfigEnvironment.MACHINE_AUTH_REMEMBER_IP) {
            // Machine Auth Remember IP
            if (
                ipAddress == "127.0.0.1" ||
                ipAddress == "0:0:0:0:0:0:0:1"
            ) {
                // Local Host
                return true
            }
            if (ClientIpWhiteList.checkIpIsInWhiteList(ipAddress)) {
                return true
            }
        }

        var errorText = "Unknown Auth Error."

        if (request.method == "POST") {
            val json = request.inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSON.parseObject(json) as JSONObject

            errorText = if (jsonObject.containsKey("accessKey")) {
                val accessKeyValue = jsonObject.getString("accessKey")
                // println("Key value: $accessKeyValue")
                val accessKeyObj = ClientAccessKey(accessKeyValue)
                if (accessKeyObj.isValid()) {
                    ClientIpWhiteList.addIpToWhiteList(ipAddress)
                    return true
                } else {
                    "AccessKey is not valid."
                }
            } else {
                "AccessKey does not exist in the JSON payload."
            }
        } else if (request.method == "GET") {
            val accessKeyValue = request.getParameter("accessKey")

            errorText = if (accessKeyValue != null) {
                val accessKeyObj = ClientAccessKey(accessKeyValue)
                if (accessKeyObj.isValid()) {
                    ClientIpWhiteList.addIpToWhiteList(ipAddress)
                    return true
                } else {
                    "AccessKey is not valid."
                }
            } else {
                "AccessKey does not exist in the GET request."
            }
        }

        val errorResponse = AuthResponse()
        errorResponse.result = errorText
        errorResponse.isAuthenticated = false
        errorResponse.isSucceed = false
        errorResponse.haveError = true
        errorResponse.ipAddress = ipAddress

        val jsonString = JSON.toJSONString(errorResponse)
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.characterEncoding = "UTF-8"
        response.contentType = "application/json"
        response.writer.write(jsonString)

        return false
    }

    override fun postHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        modelAndView: ModelAndView?
    ) {
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
    }
}
