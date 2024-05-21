package com.tuc.masters.implementations.parsers

import com.tuc.masters.core.TestParser
import com.tuc.masters.core.models.ActionType
import com.tuc.masters.core.models.EvaluatorConfig
import com.tuc.masters.core.models.InterfaceAction
import com.tuc.masters.core.models.ParsedData
import org.springframework.stereotype.Component
import java.io.File

//    val signature = Regex("\\s*public[\\sa-z]*(?<name>[0-9a-zA-Z_]*)\\([a-zA-Z0-9_,\\S]*\\)")

@Component
class JavaSeleniumTestParser : TestParser {
    override val supportedLanguages: List<String>
        get() = listOf("Java")
    override val supportedFrameworks: List<String>
        get() = listOf("Selenium")

    override fun parseFile(file: File, config: EvaluatorConfig): List<ParsedData> {
        val parsedData = mutableListOf<ParsedData>()
        val lines = file.readLines()
        val filtered = removeCommentParts(lines)
        val content = filtered.joinToString("\n")

        // first implemented option - splitting by annotation
        if (config.testAnnotation != null) {
            val testRgx = Regex(config.testAnnotation)
            val testsR = findAllEntries(content, testRgx)
            testsR.add(Pair("End", IntRange(content.length, content.length)))

            testsR.drop(1).zipWithNext().forEach {
                val start = it.first.second.first
                val end = it.second.second.first - 1

                val testCode = content.substring(start, end)
                val result = getTest(file.name, testCode)
                if (result != null) parsedData.add(result)
            }
        }

        return parsedData
    }

    private fun findAllEntries(content: String, regex: Regex): ArrayList<Pair<String, IntRange>> {
        var current = content
        val availableF = arrayListOf(Pair("Start", IntRange(0, 0)))
        while (current.isNotEmpty()) {
            val ind = getFirstEntry(current, regex)
            if (ind.first == null || ind.second == null) break
            val lastE = availableF.last()
            availableF.add(
                Pair(
                    ind.first!!,
                    IntRange(ind.second!!.first + lastE.second.first, ind.second!!.last + lastE.second.last)
                )
            )
            current = current.substring(ind.second?.last ?: 0)
        }
        return availableF
    }

    private fun getFirstEntry(content: String, regex: Regex): Pair<String?, IntRange?> {
        val result = regex.find(content)
        val i = if ((result?.groups?.size ?: 0) > 1) 1 else 0
        val methodName = result?.groups?.get(i)?.value
        return Pair(methodName, result?.range)
    }

    private fun removeCommentParts(data: List<String>): List<String> {
        val singleLine = Regex("\\s*//[\\S\\s]*")
        val multilineStart = Regex("\\s*/\\*")
        val multilineEnd = Regex("\\s*\\*/")
        val filtered = mutableListOf<String>()
        var isLongComment = false
        data.forEach {
            if (multilineStart.matches(it)) isLongComment = true
            if (it.isNotBlank() && !singleLine.matches(it) && !isLongComment) filtered.add(it)
            if (multilineEnd.matches(it)) isLongComment = false
        }

        return filtered
    }

    private fun getTest(fileName: String, testCode: String): ParsedData? {
        val signature = Regex("public void (?<name>[a-zA-Z0-9_]+)\\([a-zA-Z0-9_,\\s]*\\)[a-zA-Z\\s]*\\{")
        val end = Regex("\\}\\s+")

        val result = signature.find(testCode) ?: return null
        val code = testCode.substring(result.range.last + 1).split(end)[0].trim()

        val methodName = result.groups[0]?.value
        return ParsedData(testName = methodName ?: "unknown", fileName = fileName, actions = parseActions(code))
    }

    private fun parseActions(sourceCode: String): List<InterfaceAction> {
        val actions = mutableListOf<InterfaceAction>()
        val snippets = sourceCode.split("\n")

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