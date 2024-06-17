package com.tuc.masters.commands

import com.tuc.masters.core.UIEvaluatorController
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption
import java.util.logging.Logger

@ShellComponent
class UIEvaluatorCommand(@Autowired private val controller: UIEvaluatorController) {
    companion object : KLogging()

    // CLI command 'evaluate' triggers the calculation of the metrics
    @ShellMethod(value = "Evaluates application based on provided UI tests and config")
    fun evaluate(@ShellOption(defaultValue = "./") path: String) {
        try {
            controller.evaluate(path)
        } catch (e: Exception) {
            logger.error(e) { "Something went wrong during the process" }
        }
    }

    // CLI command 'evaluate' triggers the calculation of the metrics
    @ShellMethod(value = "Provides information about the CLI tool")
    fun info() {
        try {
            controller.info()
        } catch (e: Exception) {
            logger.error(e) { "Something went wrong during the process" }
        }
    }
}