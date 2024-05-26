package com.tuc.masters.core.models

data class TestData (
    var testName: String,
    var filePath: String?,
    var sourceCode: ParsedData,
    var logs: ParsedData?
)