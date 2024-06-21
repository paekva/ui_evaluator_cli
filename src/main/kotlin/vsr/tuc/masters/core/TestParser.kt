package vsr.tuc.masters.core

import vsr.tuc.masters.core.models.EvaluatorConfig
import vsr.tuc.masters.core.models.ParsedData
import java.io.File

// Describes the functionality that a parser for tests data should have
interface TestParser {
    val supportedLanguages: List<String>
    val supportedFrameworks: List<String> // UI testing frameworks
    fun parseFile(file: File, config: EvaluatorConfig): List<ParsedData> // one test file can include multiple tests
}
