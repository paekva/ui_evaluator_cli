package com.tuc.masters.implementations.parsers

import com.tuc.masters.core.TestParser
import com.tuc.masters.core.models.ActionType
import com.tuc.masters.core.models.EvaluatorConfig
import com.tuc.masters.core.models.InterfaceAction
import com.tuc.masters.core.models.ParsedData
import org.springframework.stereotype.Component
import java.io.File


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

        tests.forEach {
            val result = getTest(filePathParts, it)
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

    private fun getTest(filePath: String, testCode: String): ParsedData? {
        val tmp = testCode.trim().split("\n")
        val signature = Regex("function (?<name>[a-zA-Z0-9_]+)\\([a-zA-Z0-9_,\\S]*\\)[: void]?[\\S\\s]*")
        val result = signature.find(tmp[0]) ?: return null
        val methodName = result.groups[1]?.value

        return ParsedData(
            testName = methodName ?: "unknown",
            filePath = filePath,
            actions = if (tmp.count() < 4) listOf() else parseActions(
                tmp.subList(2, tmp.count() - 2).joinToString("\n")
            )
        )
    }

    private fun parseActions(sourceCode: String): List<InterfaceAction> {
        val snippets = sourceCode.split(Regex(";[\\s\n]*"))

        val actions = mutableListOf<InterfaceAction>()
        snippets.forEach { snippet ->
            if (asserts.any { snippet.contains(it) }) {
                actions.add(InterfaceAction(wholeLine = snippet, type = ActionType.ASSERT, args = null))
            } else if (snippet.contains("wait")) {
                actions.add(InterfaceAction(wholeLine = snippet, type = ActionType.WAIT, args = null))
            } else {
                actions.add(InterfaceAction(wholeLine = snippet, type = ActionType.OTHER, args = null))
            }
        }

        return actions
    }

    private val asserts: List<String> = listOf(
        "assertTrue", "assertFalse", "assertEquals", "assertNotEqual",
        "assertGreaterThan", "assertStringDoesNotContain", "assertStringContains", "assertNotMatches",
        "assertMatches", "assertContains", "assertNotNull", "assertNull", "assertLessThanEqualTo", "assertLessThan",
        "assertGreaterThanEqualTo",
    )
}