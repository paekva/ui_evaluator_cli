package vsr.tuc.masters.core.models

// All the extracted data related to a single UI test
data class TestData (
    var testName: String,
    var filePath: String?,
    var sourceCode: ParsedData,
    var logs: ParsedData?
)