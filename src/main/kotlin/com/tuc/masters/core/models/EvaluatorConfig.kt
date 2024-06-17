package com.tuc.masters.core.models

// Description of the project for the evaluation process
data class EvaluatorConfig(
    val testLanguage: String? = "Java",
    val testBrowser: String? = "GoogleChrome",
    val testFramework: String? = "Selenium",
    val projectPath: String? = null,
    val testsPath: String? = "/tests",
    val logsPath: String? = null,
    val testFilePostfix: String?,
    val testExtension: String? = null,
    val logExtension: String = "log",
    val exclude: List<String>? = listOf(),
    val testAnnotation: String? = null,
    val testPrefix: String? = "",
    val skipTestsWithoutLogs: Boolean = false,
    val visualiseSingleTestMetrics: Boolean = false,
    val groups: Map<String, List<String>>? = null,
)