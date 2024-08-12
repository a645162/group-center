package com.khm.group.center.utils.file

import org.junit.jupiter.api.Test
import java.io.File

class WalkDirTest {
    @Test
    fun testWalkDir() {
        val dirPath = "./Users"

        // Check directory exists
        if (File(dirPath).exists()) {
            println("Directory exists")
        } else {
            println("Directory does not exist")
        }

        var pathList = ProgramFile.walkFileTree(dirPath, "yaml", false)
        println(pathList.size)
        for (path in pathList) {
            println(path)
        }

        pathList = ProgramFile.walkFileTree(dirPath, "yaml", true)
        println(pathList.size)
        for (path in pathList) {
            println(path)
        }
    }
}
