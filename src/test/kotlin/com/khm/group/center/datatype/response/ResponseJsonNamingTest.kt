package com.khm.group.center.datatype.response

import com.alibaba.fastjson2.JSON
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ResponseJsonNamingTest {

    @Test
    fun `auth response keeps legacy boolean field names in jackson`() {
        val response = AuthResponse().apply {
            isAuthenticated = true
            isSucceed = true
            accessKey = "test-key"
            result = "ok"
        }

        val mapper = jacksonObjectMapper()
        val jsonText = mapper.writeValueAsString(response)
        val jsonMap: Map<String, Any?> = mapper.readValue(jsonText)

        assertTrue(jsonMap.containsKey("isAuthenticated"))
        assertTrue(jsonMap.containsKey("isSucceed"))
        assertFalse(jsonMap.containsKey("authenticated"))
        assertFalse(jsonMap.containsKey("succeed"))
    }

    @Test
    fun `auth response naming is consistent between jackson and fastjson`() {
        val response = AuthResponse().apply {
            isAuthenticated = true
            isSucceed = true
            accessKey = "test-key"
        }

        val jacksonText = jacksonObjectMapper().writeValueAsString(response)
        val fastjsonText = JSON.toJSONString(response)

        val mapper = jacksonObjectMapper()
        val jacksonMap: Map<String, Any?> = mapper.readValue(jacksonText)
        val fastjsonMap: Map<String, Any?> = mapper.readValue(fastjsonText)

        assertTrue(jacksonMap.containsKey("isAuthenticated"))
        assertTrue(jacksonMap.containsKey("isSucceed"))
        assertTrue(fastjsonMap.containsKey("isAuthenticated"))
        assertTrue(fastjsonMap.containsKey("isSucceed"))
    }
}
