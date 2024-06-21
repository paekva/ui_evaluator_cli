package vsr.tuc.masters.core

import vsr.tuc.masters.core.models.EvaluatorConfig
import vsr.tuc.masters.core.models.InterfaceAction
import vsr.tuc.masters.core.models.ParsedData
import java.io.File

// Describes the functionality that a parser for logging data should have
interface LogParser {
    val supportedBrowsers: List<String>
    fun parseFile(file: File, config: EvaluatorConfig): ParsedData
}