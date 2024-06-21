package vsr.tuc.masters.implementations.parsers

import vsr.tuc.masters.core.LogParser
import vsr.tuc.masters.core.models.ActionType
import vsr.tuc.masters.core.models.EvaluatorConfig
import vsr.tuc.masters.core.models.InterfaceAction
import vsr.tuc.masters.core.models.ParsedData
import org.springframework.stereotype.Component
import java.io.File


// Implementation of the [LogParser] for GoogleChrome based logs
@Component
class ChromeLogParser : LogParser {
    override val supportedBrowsers: List<String>
        get() = listOf("GoogleChrome")

    override fun parseFile(file: File, config: EvaluatorConfig): ParsedData {
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
            var type = getActionType(it.first.second)
            val responseType = getActionType(it.second.second)
            val args = if (type != ActionType.OTHER) getArguments(it.first.second, it.second.second) else null

            if (type == ActionType.SCRIPT) {
                if (args?.contains("isDisplayed") == true) type = ActionType.IS_DISPLAYED
                if (args?.contains("scrollIntoView") == true) type = ActionType.SCROLL
            }

            interactions.add(
                InterfaceAction(
                    wholeLine = "${it.first.first} ${it.first.second}",
                    type = type,
                    timestamp = timestamp,
                    args = args,
                    hasError = responseType == ActionType.ERROR
                )
            )
        }

        return ParsedData(file.name.split(".log")[0], file.path, interactions, content)
    }

    private fun getArguments(command: String, response: String): String {
        var commandSplit = command.trim().split("\n")
        commandSplit = if (commandSplit.size < 2) listOf() else commandSplit.slice(1..<commandSplit.size - 1)
        var responseSplit = response.trim().split("\n")
        responseSplit = if (responseSplit.size < 2) listOf() else responseSplit.slice(1..<responseSplit.size - 1)
        return (commandSplit + responseSplit).joinToString("\n") { it.trim() }
    }

    private fun getActionType(value: String): ActionType {
        val errorRgx = Regex("\\[[(0-9|a-z)]+\\] (COMMAND|RESPONSE) (?<action>[(a-z|A-Z)]+) ERROR")
        val errorResult = errorRgx.find(value)
        if (errorResult != null) return ActionType.ERROR

        val r1 = Regex("\\[[(0-9|a-z)]+\\] (COMMAND|RESPONSE) (?<action>[(a-z|A-Z)]+)")
        val result = r1.find(value)
        val action = result?.groups?.get(2)?.value ?: ""

        return mapStringToActionType[action] ?: ActionType.OTHER
    }

    private val mapStringToActionType = mapOf(
        "InitSession" to ActionType.START,
        "Quit" to ActionType.STOP,
        "Navigate" to ActionType.LINK,
        "FindElement" to ActionType.FIND,
        "FindChildElement" to ActionType.FIND,
        "FindElements" to ActionType.FIND_ALL,
        "FindChildElements" to ActionType.FIND_ALL,
        "ExecuteScript" to ActionType.SCRIPT,
        "TypeElement" to ActionType.SEND_KEYS,
        "ClickElement" to ActionType.CLICK,
        "ClearElement" to ActionType.CLICK,
        "IsElementDisplayed" to ActionType.IS_DISPLAYED,
        "IsElementEnabled" to ActionType.IS_DISPLAYED,
        "GetElementText" to ActionType.GET_PROPERTY,
        "GetElementProperty" to ActionType.GET_PROPERTY,
        "GetElementCSSProperty" to ActionType.GET_PROPERTY,
        "PerformActions" to ActionType.SCRIPT,
        "Refresh" to ActionType.GET_PROPERTY,
        "GetAlertMessage" to ActionType.SCRIPT,
        "GetUrl" to ActionType.LINK,
        "GetElementRect" to ActionType.GET_PROPERTY,
    )
}