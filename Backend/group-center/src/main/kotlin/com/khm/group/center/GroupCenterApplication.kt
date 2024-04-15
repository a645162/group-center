package com.khm.group.center

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
class GroupCenterApplication {

    final var logger: Logger = LoggerFactory.getLogger(javaClass)

    init {
        logger.info("GroupCenterApplication init")
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<GroupCenterApplication>(*args)
        }
    }
}
