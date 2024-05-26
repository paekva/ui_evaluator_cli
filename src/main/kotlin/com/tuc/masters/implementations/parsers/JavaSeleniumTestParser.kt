package com.tuc.masters.implementations.parsers

import com.tuc.masters.core.TestParser
import com.tuc.masters.core.models.EvaluatorConfig
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
        val signature = Regex("void (?<name>[a-zA-Z0-9_]+)\\([a-zA-Z0-9_,\\s]*\\)[a-zA-Z\\s,]*\\{")

        li.forEach {
            val result = getTest(filePathParts, it, signature)
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
}