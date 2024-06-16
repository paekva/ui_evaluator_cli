package com.tuc.masters.core.models

// Information about tests group
data class GroupData (
    var groupName: String,
    var tests: List<TestData>
)