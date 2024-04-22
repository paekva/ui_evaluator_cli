package com.tuc.masters.metrics.models

data class TestData (
    var testName: String,
    var actions: List<InterfaceAction> // ideally setup and teardown code is also here for tests
)