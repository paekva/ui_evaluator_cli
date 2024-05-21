package com.tuc.masters.core.models

data class TestData (
    var testName: String,
    var fileName: String?,
    var sourceCode: List<InterfaceAction>,
    var logs: List<InterfaceAction>
)