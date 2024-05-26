package com.tuc.masters.implementations.parsers

import com.tuc.masters.core.TestParser
import com.tuc.masters.core.models.ActionType
import com.tuc.masters.core.models.EvaluatorConfig
import com.tuc.masters.core.models.InterfaceAction
import com.tuc.masters.core.models.ParsedData
import org.springframework.stereotype.Component
import java.io.File

@Component
class JavaSeleniumTestParser : TestParser {
    override val supportedLanguages: List<String>
        get() = listOf("Java")
    override val supportedFrameworks: List<String>
        get() = listOf("Selenium")

    override fun parseFile(file: File, config: EvaluatorConfig): List<ParsedData> {
        val filePathParts = file.path.split(config.projectPath ?: "")[1]
        val parsedData = mutableListOf<ParsedData>()
        val lines = file.readLines()
        val filtered = removeCommentParts(lines)
        val content = filtered.joinToString("\n")

        // first implemented option - splitting by annotation
        val li = findAllEntries(content, config.testAnnotation ?: config.testPrefix)
        li.forEach {
            val result = getTest(filePathParts, it)
            if (result != null) parsedData.add(result)
        }

        return parsedData
    }

    private fun findAllEntries(content: String, key: String?): List<String> {
        if (key == null) return listOf()

        val indexes = arrayListOf<Int>()
        var cc = content
        while (cc.isNotEmpty()) {
            val res = cc.indexOf(key)
            if (res != -1) {
                indexes.add(res)
                cc = cc.substring(res + 1)
            } else break
        }
        if (indexes.isEmpty()) return listOf()

        val newI = arrayListOf(indexes.first())
        indexes.drop(1).forEach { newI.add(newI.last() + it + 1) }

        val parts = arrayListOf<String>()
        var counter = 0
        while (counter < newI.size) {
            val newV = if (counter + 1 < newI.size) newI[counter + 1] else (content.length - 1)
            parts.add(content.substring(newI[counter], newV))
            counter++
        }
        return parts
    }

    private fun removeCommentParts(data: List<String>): List<String> {
        val singleLine = Regex("\\s*//[\\S\\s]*")
        val multilineStart = Regex("\\s*/[*]+")
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

    private fun getTest(filePath: String, testCode: String): ParsedData? {
        val signature = Regex("void (?<name>[a-zA-Z0-9_]+)\\([a-zA-Z0-9_,\\s]*\\)[a-zA-Z\\s,]*\\{")

        val result = signature.find(testCode) ?: return null
        val start = result.range.last + 1
        val end = start + findTestEnd(testCode.substring(start))
        val code = testCode.substring(start, end - 1)

        val methodName = result.groups[1]?.value
        return ParsedData(testName = methodName ?: "unknown", filePath = filePath, actions = parseActions(code))
    }


    private fun findTestEnd(content: String): Int {
        var openBracketCount = 1
        var index = 0
        val tmp = content.toCharArray()
        while (openBracketCount > 0) {
            val it = tmp[index]
            if (it == '{') {
                openBracketCount++
            } else if (it == '}') {
                openBracketCount--
            }
            index++
        }
        return index
    }

    private fun parseActions(sourceCode: String): List<InterfaceAction> {
        val actions = mutableListOf<InterfaceAction>()
        val snippets = arrayListOf<String>()
        sourceCode.trim().split(";\n").map { it.replace("\t", "") }
            .forEach { s ->
                if (s.count { it == '{' } != s.count { it == '}' }) {
                    val kk = clean(s)
                    snippets.addAll(kk)
                } else snippets.add(s)
            }

        snippets.filter { it.isNotEmpty() }.forEach { snippet ->
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

    private fun clean(str: String): List<String> {
        val l = str.replace("\n", "")
        if (l.count { it == '{' } < l.count { it == '}' }) {
            return if (l.trim().count() == 1 && l.trim() == "}") arrayListOf("")
            else {
                var count = 0
                var r = ""
                l.toCharArray().forEach {
                    if (it == '}') {
                        if (count > 0) count--
                        else r += ""
                    } else {
                        if (it == '{') count++
                        r += it
                    }
                }
                arrayListOf(r)
            }
        }

        val fR = Regex("^\\s*for\\s*\\([^{}()]*\\)\\s\\{")
        if (fR.find(l) != null) {
            val tmp = fR.find(l)?.range?.last ?: -1
            return checkRest(tmp + 1, l)
        }

        val wR = Regex("^\\s*while\\s*\\([^{}()]*\\)\\s\\{")
        if (wR.find(l) != null) {
            val tmp = wR.find(l)?.range?.last ?: -1
            return checkRest(tmp + 1, l)
        }

        val ifR = Regex("^\\s*if\\s*(?<name>\\([\\S\\s]*\\))\\s\\{")
        if (ifR.find(l) != null) {
            val tmp = ifR.find(l)
            val inner = tmp?.groups?.get(1)?.value?.trim()?.drop(1)?.dropLast(1) ?: ""
            return arrayListOf(inner) + checkRest((tmp?.range?.last ?: -1) + 1, l)
        }

        val eR = Regex("else\\s*\\{")
        if (eR.find(l) != null) {
            val tmp = eR.find(l)?.range?.last ?: -1
            return checkRest(tmp + 1, l)
        }

        return arrayListOf(l)
    }

    private fun checkRest(index: Int, content: String): List<String> {
        val rest = content.substring(index).trim().replace("\t", "")
        return clean(rest)
    }

    private val asserts: List<String> = listOf(
        "assertTrue", "assertFalse", "assertEquals", "assertNotEqual",
        "assertGreaterThan", "assertStringDoesNotContain", "assertStringContains", "assertNotMatches",
        "assertMatches", "assertContains", "assertNotNull", "assertNull", "assertLessThanEqualTo", "assertLessThan",
        "assertGreaterThanEqualTo",
    )
}