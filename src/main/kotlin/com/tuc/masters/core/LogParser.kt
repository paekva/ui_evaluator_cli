package com.tuc.masters.core

import com.tuc.masters.core.models.InterfaceAction
import java.io.File

interface ArtifactsParser {
    fun parseFile(file: File): List<InterfaceAction>
}

interface LogParser: ArtifactsParser {
    val supportedBrowsers: List<String>
}

interface TestParser: ArtifactsParser {
    var supportedLanguages: List<String>
    // UI testing frameworks
    val supportedFrameworks: List<String>
}
