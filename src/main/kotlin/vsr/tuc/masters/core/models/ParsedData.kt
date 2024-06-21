package vsr.tuc.masters.core.models

// Data, extracted from a single source of information: log or test
data class ParsedData (
    var testName: String,
    var filePath: String?,
    var actions: List<InterfaceAction>,
    var rawData: String,
)