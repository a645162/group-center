package com.khm.group.center.controller.api.client.file

import com.khm.group.center.config.env.ConfigEnvironment
import com.khm.group.center.datatype.config.GroupUserConfig
import com.khm.group.center.utils.program.Slf4jKt
import com.khm.group.center.utils.program.Slf4jKt.Companion.logger
import io.swagger.v3.oas.annotations.Operation
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Slf4jKt
@RestController
class SshKeyController {

    fun checkUserNameEngIsValid(userNameEng: String): Boolean {
        return (
                GroupUserConfig.getUserByNameEng(userNameEng) != null
                        || userNameEng == "root"
                )
    }

    @Operation(summary = "用户上传SSH秘钥文件")
    @RequestMapping(
        "/api/client/file/ssh_key",
        method = [RequestMethod.POST],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun postSshFileUpload(
        @RequestPart("file") file: MultipartFile,
        userNameEng: String
    ): ResponseEntity<String> {
        try {
            val fileName =
                file.originalFilename ?: return ResponseEntity.badRequest().body("Filename is null.")

            if (!checkUserNameEngIsValid(userNameEng)) {
                return ResponseEntity.badRequest().body("User not found.")
            }

            logger.info("Receive User($userNameEng) FileName($fileName)")
            if (fileName == "authorized_keys") {
                val fileContent = file.bytes.toString(Charsets.UTF_8)
                receiveAuthorizedKeys(userNameEng, fileContent)
            } else if (fileName == "ssh_key_pair.zip") {
                val userFileDirPath = getUserFileDirectory(userNameEng)
                val destFilePath = "$userFileDirPath/$fileName"
                val destPaths = Paths.get(destFilePath).toAbsolutePath()

                file.transferTo(destPaths)
            } else {
                return ResponseEntity.badRequest().body("Invalid file name.")
            }

            return ResponseEntity.ok("File ($fileName) uploaded successfully.")
        } catch (e: Exception) {
            return ResponseEntity.badRequest().body("Failed to upload file: ${e.message}")
        }
    }

    @RequestMapping(path = ["/api/client/file/ssh_key/{filename:.+}"], method = [RequestMethod.GET])
    @Throws(IOException::class)
    fun getSshKeyFile(
        @PathVariable filename: String,
        userNameEng: String,
        response: HttpServletResponse
    ) {
        if (!checkUserNameEngIsValid(userNameEng)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }

        val userFileDirPath = getUserFileDirectory(userNameEng)
        val file = File("$userFileDirPath/$filename")
        val path: Path = Paths.get(file.absolutePath)

        if (Files.exists(path)) {
            if (file.isDirectory) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN)
                return
            }

            response.setHeader("Content-Disposition", "attachment; filename=" + file.name)
            response.setContentLength(file.length().toInt())
            response.contentType = "application/octet-stream"

            Files.newInputStream(path).use { inputStream ->
                response.outputStream.use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
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

        return userFileDirPath
    }

    fun receiveAuthorizedKeys(userNameEng: String, content: String) {
        val userDir = getUserFileDirectory(userNameEng)
        val authorizedKeysFilePath = "$userDir/authorized_keys"
        val authorizedKeysFile = AuthorizedKeysFile(content)

        // Check Old File is existing
        val file = File(authorizedKeysFilePath)
//        if (file.exists()) {
//            val oldFileContent = file.readText()
//            val oldAuthorizedKeysFile = AuthorizedKeysFile(oldFileContent)
//            authorizedKeysFile.combine(oldAuthorizedKeysFile)
//        }

        // Write to file
        file.writeText(authorizedKeysFile.build())
    }
}
