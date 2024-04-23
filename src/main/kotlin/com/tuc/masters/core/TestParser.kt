package com.tuc.masters.core

import com.tuc.masters.core.models.TestData
import java.io.File


interface TestParser {
    val supportedLanguages: List<String>
    // UI testing frameworks
    val supportedFrameworks: List<String>
    fun parseFile(file: File): List<TestData> // one test file can include multiple tests
}
