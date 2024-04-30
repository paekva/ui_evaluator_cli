package com.tuc.masters.core.models

data class EvaluatorConfig(
    val path: String = "./",
    val testsPath: String = "/tests",
    val logsPath: String = "/logs",
    val testFilePostfix: String?,
    val testExtension: String? = null,
    val logExtension: String = "log",
    val exclude: List<String>? = listOf(),
    val testAnnotation: String? = null
)