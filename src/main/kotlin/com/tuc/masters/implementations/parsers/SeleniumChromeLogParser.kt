package com.tuc.masters.implementations.parsers

import com.tuc.masters.core.LogParser
import com.tuc.masters.core.models.ActionType
import com.tuc.masters.core.models.EvaluatorConfig
import com.tuc.masters.core.models.InterfaceAction
import org.springframework.stereotype.Component
import java.io.File


@Component
class SeleniumChromeLogParser : LogParser {
    override val supportedBrowsers: List<String>
        get() = listOf("GoogleChrome")

    override fun parseFile(file: File, config: EvaluatorConfig): List<InterfaceAction> {
        val interactions = mutableListOf<InterfaceAction>()

        val content = file.readText().trim()
        val regex = Regex("\\[[0-9\\|.]+\\]\\[[A-Z]+\\]:")
        val keys = regex.findAll(content).map { it.value }.toList()
        val values = regex.split(content).toList().drop(1)
        val results = mutableListOf<Pair<String, String>>()

        keys.forEachIndexed { index, matchResult ->
            if (values[index].contains("COMMAND") || values[index].contains("RESPONSE")) {
                results.add(Pair(matchResult, values[index]))
            }
        }
        val tmp = results.zipWithNext().filter { it.first.second.contains("COMMAND") }

        tmp.forEach {
            val r2 = Regex("\\[[0-9|.]+\\]")
            val timestamp = r2.find(it.first.first)?.value?.drop(1)?.dropLast(1)
            val type = getActionType(it.first.second)
            val args = if (type != ActionType.OTHER) getArguments(it.first.second, it.second.second) else null
            interactions.add(
                InterfaceAction(
                    wholeLine = "${it.first} ${it.second}",
                    type = type,
                    timestamp = timestamp,
                    args = args
                )
            )
        }

        return interactions

    }

    private fun getArguments(command: String, response: String): Any {
        var commandSplit = command.trim().split("\n")
        commandSplit = if (commandSplit.size < 2) listOf() else commandSplit.slice(1..<commandSplit.size - 1)
        var responseSplit = response.trim().split("\n")
        responseSplit = if (responseSplit.size < 2) listOf() else responseSplit.slice(1..<responseSplit.size - 1)
        return (commandSplit + responseSplit).joinToString("\n") { it.trim() }
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