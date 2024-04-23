package com.tuc.masters.core

import com.tuc.masters.core.models.InterfaceAction
import com.tuc.masters.core.models.TestData
import java.io.File

interface LogParser {
    val supportedBrowsers: List<String>
    fun parseFile(file: File): List<InterfaceAction>
}