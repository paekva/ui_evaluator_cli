package com.tuc.masters.core.models

// Information about a single extracted from the test action
class InterfaceAction (
    var wholeLine: String,
    var type: ActionType,
    var timestamp: String? = null,
    var args: String?,
    var hasError: Boolean = false
)

