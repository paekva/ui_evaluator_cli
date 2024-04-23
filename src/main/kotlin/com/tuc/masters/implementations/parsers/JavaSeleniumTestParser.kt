package com.tuc.masters.implementations.parsers

import com.tuc.masters.core.TestParser
import com.tuc.masters.core.models.TestData
import org.springframework.stereotype.Component
import java.io.File


@Component
class JavaSeleniumTestParser: TestParser {
    override val supportedLanguages: List<String>
        get() = listOf("Java")
    override val supportedFrameworks: List<String>
        get() = listOf("Selenium")
    override fun parseFile(file: File): List<TestData> {
        val testData = mutableListOf<TestData>()

        // we need config to see how to identify tests annotations or name regex
        // also for white or black listing for specific tests by names
        return testData
    }
}