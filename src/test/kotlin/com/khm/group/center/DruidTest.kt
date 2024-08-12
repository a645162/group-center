package com.khm.group.center

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import javax.sql.DataSource

@SpringBootTest
class DruidTest {

    @Autowired
    private lateinit var dataSource: DataSource

    @Test
    fun test() {
        println("[Test]dataSource.javaClass")
        println(dataSource.javaClass)
    }

}
