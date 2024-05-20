package com.tuc.masters.core.models

data class EvaluatorConfig(
    val projectPath: String? = null,
    val testsPath: String? = "/tests",
    val logsPath: String? = null,
    val testFilePostfix: String?,
    val testExtension: String? = null,
    val logExtension: String = "log",
    val exclude: List<String>? = listOf(),
    val testAnnotation: String? = null,
    val groups: Map<String, List<String>>? = null
)