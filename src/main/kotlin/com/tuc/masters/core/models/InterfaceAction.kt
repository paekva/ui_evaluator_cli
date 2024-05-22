package com.tuc.masters.core.models

class InterfaceAction (
    var wholeLine: String,
    var type: ActionType,
    var timestamp: String? = null,
    var args: String?,
    var hasError: Boolean = false
)

