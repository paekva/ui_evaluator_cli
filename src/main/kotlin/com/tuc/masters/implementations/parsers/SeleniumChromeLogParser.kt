package com.tuc.masters.implementations.parsers

import com.tuc.masters.core.LogParser
import com.tuc.masters.core.models.ActionType
import com.tuc.masters.core.models.InterfaceAction
import org.springframework.stereotype.Component
import java.io.File


@Component
class SeleniumChromeLogParser: LogParser {
    override val supportedBrowsers: List<String>
        get() = listOf("GoogleChrome")
    override fun parseFile(file: File): List<InterfaceAction> {
        val interactions = mutableListOf<InterfaceAction>()

        val content = file.readText().trim()
        val regex = Regex("\\[[0-9\\|.]+\\]\\[[A-Z]+\\]:")
        val keys = regex.findAll(content).map { it.value }.toList()
        val values = regex.split(content).toList().drop(1)
        val results = mutableListOf<Pair<String, String>>()

        keys.forEachIndexed { index, matchResult ->
            results.add(Pair(matchResult, values[index]))
        }

        results.forEach{
            val r2 = Regex("\\[[0-9|.]+\\]")
            val timestamp = r2.find(it.first)?.value?.drop(1)?.dropLast(1)
            val type = getActionType(it.second)
            val args = if (type != ActionType.OTHER) getArguments(it.second) else mapOf()
            interactions.add(InterfaceAction(
                wholeLine = "${it.first} ${it.second}",
                type = type,
                timestamp = timestamp,
                args = args
            ))
        }

        return mutableListOf()

    }

    private fun getArguments(value: String): Map<String, String> {
        val args = mutableMapOf<String, String>()
        val r1 = Regex("\\{(.|\n)*\\}")
        val argsString = r1.find(value)?.value ?: ""

        val r2 = Regex("\n\\s\\s\\s\"(?<key>[a-z]+)\": (?<value>(.|\n)+)")
        r2.findAll(argsString).map {
            val g = it.groups
            val key = g[1]?.value ?: ""
            args[key] = g[2]?.value ?: ""
        }.toList()

        return args
    }

    private fun getActionType(value: String): ActionType {
        val r1 = Regex("\\[[(0-9|a-z)]+\\] (COMMAND|RESPONSE) (?<action>[(a-z|A-Z)]+)")
        val result = r1.find(value)
        val type = result?.groups?.get(1)?.value ?: ""
        val action = result?.groups?.get(2)?.value ?: ""
        val actionType = mapStringToActionType[action]

        return if (type == "COMMAND" && actionType != null) actionType else ActionType.OTHER
    }

    private val mapStringToActionType = mapOf(
        "InitSession" to ActionType.START,
        "Quit" to ActionType.STOP,
        "SetWindowRect" to ActionType.OTHER,
        "Navigate" to ActionType.LINK,
        "FindElement" to ActionType.FIND,
        "FindElements" to ActionType.FIND_ALL,
        "ExecuteScript" to ActionType.SCRIPT,
        "TypeElement" to ActionType.SEND_KEYS,
        "ClickElement" to ActionType.CLICK,
    )
}