package com.tuc.masters.core.models

// Information about a single action, extracted from the test
class InterfaceAction (
    var wholeLine: String,
    var type: ActionType,
    var timestamp: String? = null,
    var args: String?,
    var hasError: Boolean = false
)

