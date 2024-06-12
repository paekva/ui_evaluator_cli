package com.tuc.masters.core

import com.tuc.masters.core.models.EvaluatorConfig
import com.tuc.masters.core.models.ParsedData
import java.io.File


interface TestParser {
    val supportedLanguages: List<String>
    val supportedFrameworks: List<String> // UI testing frameworks
    fun parseFile(file: File, config: EvaluatorConfig): List<ParsedData> // one test file can include multiple tests
}
