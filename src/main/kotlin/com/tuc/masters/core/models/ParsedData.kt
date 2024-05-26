package com.tuc.masters.core.models

data class ParsedData (
    var testName: String,
    var filePath: String?,
    var actions: List<InterfaceAction>,
    var rawData: String,
)