package com.khm.group.center.controller.api.client.file

import java.io.File

import io.swagger.v3.oas.annotations.Operation

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

import com.khm.group.center.config.env.ConfigEnvironment
import com.khm.group.center.datatype.config.GroupUserConfig

@RestController
class SshKeyController {

    @Operation(summary = "用户上传SSH秘钥文件")
    @RequestMapping(
        "/api/client/file/ssh_key",
        method = [RequestMethod.POST],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun sshFileUpload(
        @RequestPart("file") file: MultipartFile,
        userNameEng: String
    ): ResponseEntity<String> {
        try {
            val fileName =
                file.originalFilename ?: return ResponseEntity.badRequest().body("Filename is null.")

            GroupUserConfig.getUserByNameEng(userNameEng) ?: return ResponseEntity.badRequest().body("User not found.")

            println("User Name: $userNameEng")

            println("File Name: $fileName")
            println("File Size: ${file.size}")

            // print file content
            val fileContent = file.bytes.toString(Charsets.UTF_8)
//            println("File Content: $fileContent")

            if (fileName == "authorized_keys") {
                receiveAuthorizedKeys(userNameEng, fileContent)
            }

            return ResponseEntity.ok("File ($fileName) uploaded successfully.")
        } catch (e: Exception) {
            return ResponseEntity.badRequest().body("Failed to upload file: ${e.message}")
        }
    }

    fun getUserFileDirectory(userNameEng: String): String {
        val userFileDirPath =
            ConfigEnvironment.USER_FILE_SAVE_PATH + "/$userNameEng"

        // Create directory if not exists
        val userFileDir = File(userFileDirPath)
        if (!userFileDir.exists()) {
            userFileDir.mkdirs()
        }

        println("UserFileDirectory:$userFileDir")

        return userFileDirPath
    }

    fun receiveAuthorizedKeys(userNameEng: String, content: String) {
        val userDir = getUserFileDirectory(userNameEng)
        val authorizedKeysFilePath = "$userDir/authorized_keys"
        val authorizedKeysFile = AuthorizedKeysFile(content)

        // Check Old File is existing
        val file = File(authorizedKeysFilePath)
        if (file.exists()) {
            val oldFileContent = file.readText()
            val oldAuthorizedKeysFile = AuthorizedKeysFile(oldFileContent)
            authorizedKeysFile.combine(oldAuthorizedKeysFile)
        }

        // Write to file
        file.writeText(authorizedKeysFile.build())
    }
}
