package com.tuc.masters.implementations.parsers

import com.tuc.masters.core.TestParser
import com.tuc.masters.core.models.ActionType
import com.tuc.masters.core.models.EvaluatorConfig
import com.tuc.masters.core.models.InterfaceAction
import com.tuc.masters.core.models.TestData
import org.springframework.stereotype.Component
import java.io.File


@Component
class JavaSeleniumTestParser : TestParser {
    override val supportedLanguages: List<String>
        get() = listOf("Java")
    override val supportedFrameworks: List<String>
        get() = listOf("Selenium")

    override fun parseFile(file: File, config: EvaluatorConfig): List<TestData> {
        val testData = mutableListOf<TestData>()

        // remove commented parts
        val lines = file.readLines()
        val oneLine = Regex("\\s*//[\\S\\s]*")
        val multilineStart = Regex("\\s*/\\*\\*")
        val multilineEnd = Regex("\\s*\\*/")
        val filtered = mutableListOf<String>()
        var isLongComment = false
        lines.forEach {
            if (multilineStart.matches(it)) isLongComment = true
            if (!oneLine.matches(it) && !isLongComment && it.isNotBlank()) filtered.add(it)
            if (multilineEnd.matches(it)) isLongComment = false
        }

        // first implemented option - splitting by annotation
        if (config.testAnnotation != null) {
            val content = filtered.joinToString("\n")
            val tests = content.split(config.testAnnotation)

            tests.forEach {
                val result = getTest(it)
                if (result != null) testData.add(result)
            }
        }

        return testData
    }

    private fun getTest(testCode: String): TestData? {
        // tmp assuming tests are public and void
        val signature = Regex("public void (?<name>[a-zA-Z0-9_]+)\\([a-zA-Z0-9_,\\S]*\\)[\\S\\s]*\\{")
        val end = Regex("\\}\\s+")

        val result = signature.find(testCode) ?: return null

        val match = result.value
        val methodName = result.groups[1]?.value

        val code = testCode.split(match)[1].split(end)[0].trim()

        return TestData(testName = methodName ?: "unknown", actions = parseActions(code))
    }

    private fun parseActions(sourceCode: String): List<InterfaceAction> {
        return sourceCode.split(Regex(";[\\s\n]*"))
            .map { InterfaceAction(wholeLine = it, type = ActionType.OTHER, args = mutableMapOf() ) }
    }
}