package com.tuc.masters.core.models

data class TestData (
    var testName: String,
    var filePath: String?,
    var sourceCode: List<InterfaceAction>,
    var logs: List<InterfaceAction>
)