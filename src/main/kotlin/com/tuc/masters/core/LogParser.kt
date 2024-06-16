package com.tuc.masters.core

import com.tuc.masters.core.models.EvaluatorConfig
import com.tuc.masters.core.models.InterfaceAction
import com.tuc.masters.core.models.ParsedData
import java.io.File

// Describes the functionality that a parser for logging data should have
interface LogParser {
    val supportedBrowsers: List<String>
    fun parseFile(file: File, config: EvaluatorConfig): ParsedData
}