package com.tuc.masters.implementations.parsers

import com.tuc.masters.core.TestParser
import com.tuc.masters.core.models.EvaluatorConfig
import com.tuc.masters.core.models.ParsedData
import org.springframework.stereotype.Component
import java.io.File

// Implementation of the [TestParser] for PHP + Selenium based tests
@Component
class PhpSeleniumTestParser : TestParser {
    override val supportedLanguages: List<String>
        get() = listOf("Php")
    override val supportedFrameworks: List<String>
        get() = listOf("Selenium")

    override fun parseFile(file: File, config: EvaluatorConfig): List<ParsedData> {
        val parsedData = mutableListOf<ParsedData>()
        val lines = file.readLines()
        val filtered = removeCommentParts(lines)
        val content = filtered.joinToString("\n")
        val filePathParts = file.path.split(config.projectPath ?: "")[1]
        val tests = getAllTestCodes(content)

        val signature = Regex("function (?<name>[a-zA-Z0-9_]+)\\([a-zA-Z0-9_,\\S]*\\)[: void]?[^{]*\\{")
        tests.forEach {
            val result = getTest(filePathParts, it, signature)
            if (result != null) parsedData.add(result)
        }

        return parsedData
    }

    private fun getAllTestCodes(code: String): List<String> {
        var current = code
        val indexes = mutableListOf(0)
        while (current.isNotEmpty()) {
            val ind = getNextMatchIndex(current)
            if (ind == 0) break
            indexes.add(indexes.last() + ind + 1)
            current = current.substring(ind + 1)
        }
        indexes.add(code.length - 1)

        val tests = mutableListOf<String>()
        indexes.zipWithNext().forEach { (first, second) ->
            if (first != 0) {
                tests.add(code.substring(first, second - 1))
            }
        }

        return tests
    }

    private fun getNextMatchIndex(code: String): Int {
        val signature =
            Regex("\n\\s*public function test(?<name>[a-zA-Z0-9_]+)\\([a-zA-Z0-9_,\\S]*\\)[: void]?[\\S\\s\n]*\\{")
        val result = signature.find(code)
        return result?.range?.first ?: 0
    }

    private fun removeCommentParts(data: List<String>): List<String> {
        val singleLine = Regex("\\s*//[\\S\\s]*")
        val multilineStart = Regex("\\s*/\\*\\*")
        val multilineEnd = Regex("\\s*\\*/")
        val filtered = mutableListOf<String>()
        var isLongComment = false
        data.forEach {
            if (multilineStart.matches(it)) isLongComment = true
            if (!singleLine.matches(it) && !isLongComment && it.isNotBlank()) filtered.add(it)
            if (multilineEnd.matches(it)) isLongComment = false
        }

        return filtered
    }
}