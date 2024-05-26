package com.tuc.masters.implementations.parsers

import com.tuc.masters.core.models.ActionType
import com.tuc.masters.core.models.InterfaceAction
import com.tuc.masters.core.models.ParsedData

fun getTest(filePath: String, fullCode: String, signature: Regex): ParsedData? {
    val result = signature.find(fullCode) ?: return null
    val start = result.range.last + 1
    val end = start + findTestEnd(fullCode.substring(start))
    val testCode = fullCode.substring(start, end - 1)

    val methodName = result.groups[1]?.value
    val actions = parseActions(testCode)
    return ParsedData( methodName ?: "unknown", filePath, actions, testCode )
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
            actions.add(InterfaceAction(type = ActionType.ASSERT, args = null, wholeLine = snippet))
        } else if (snippet.contains("wait")) {
            actions.add(InterfaceAction(type = ActionType.WAIT, args = null, wholeLine = snippet))
        } else {
            actions.add(InterfaceAction(type = ActionType.OTHER, args = null, wholeLine = snippet))
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
            clean(r)
        }
    }

    val regexes = listOf(
        Regex("^\\s*for\\s*\\([^{}()]*\\)\\s\\{"),
        Regex("^\\s*foreach\\s*\\([^{}()]*\\)\\s\\{"),
        Regex("^\\s*while\\s*\\([^{}()]*\\)\\s\\{"),
        Regex("^\\s*try[^{}()]*\\{"),
        Regex("^\\s*catch\\s*\\([^{}()]*\\)\\s\\{"),
        Regex("^\\s*else[^{}]*\\{")
    )

    regexes.forEach {
        val res = splitByMatch(it, l)
        if (res != null) return res
    }

    val ifR = Regex("^\\s*if\\s*(?<name>\\([\\S\\s]*\\))\\s\\{")
    if (ifR.find(l) != null) {
        val tmp = ifR.find(l)
        val inner = tmp?.groups?.get(1)?.value?.trim()?.drop(1)?.dropLast(1) ?: ""
        return arrayListOf(inner) + checkRest((tmp?.range?.last ?: -1) + 1, l)
    }

    return arrayListOf(l)
}

private fun splitByMatch(regex: Regex, content: String): List<String>?{
    val res = regex.find(content)
    if (res != null) {
        val last = res.range.last
        return arrayListOf(content.substring(0, last)) + checkRest(last + 1, content)
    }
    return null
}

private fun checkRest(index: Int, content: String): List<String> {
    val rest = content.substring(index).trim().replace("\t", "")
    return clean(rest)
}

private val asserts: List<String> = listOf(
    "assertTrue", "assertFalse", "assertEquals", "assertNotEqual", "assertNotEmpty",
    "assertGreaterThan", "assertStringDoesNotContain", "assertStringContains", "assertNotMatches",
    "assertMatches", "assertContains", "assertNotNull", "assertNull", "assertLessThanEqualTo", "assertLessThan",
    "assertGreaterThanEqualTo",
)