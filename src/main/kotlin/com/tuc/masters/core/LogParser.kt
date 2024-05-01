package com.tuc.masters.core

import com.tuc.masters.core.models.EvaluatorConfig
import com.tuc.masters.core.models.InterfaceAction
import java.io.File

interface LogParser {
    val supportedBrowsers: List<String>
    fun parseFile(file: File, config: EvaluatorConfig): List<InterfaceAction>
}