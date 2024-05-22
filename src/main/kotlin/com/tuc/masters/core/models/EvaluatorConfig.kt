package com.tuc.masters.core.models

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
    val skipTestsWithoutLogs: Boolean = false,
    val groups: Map<String, List<String>>? = null
)