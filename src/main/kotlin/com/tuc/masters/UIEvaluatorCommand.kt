package com.tuc.masters

import com.tuc.masters.core.UIEvaluatorController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption
import java.util.logging.Logger

@ShellComponent
class UIEvaluatorCommand(
    @Autowired private val controller: UIEvaluatorController,
) {
    private var log: Logger = Logger.getLogger(UIEvaluatorCommand::class.java.getName())

    @ShellMethod(value = "Validates provided config")
    fun validate(
        @ShellOption(defaultValue = "./ui_evaluator.config.yaml") configPath: String?,
    ) {
        // here check if config is available
        // if paths there are available
        // if we have support for mentioned language, UI framework and Browser
        // if we have support for selected visualisation
    }

    @ShellMethod(value = "Evaluates application based on provided UI tests and config")
    fun evaluate(@ShellOption(defaultValue = "./") path: String) {
        controller.evaluate(path)
    }
}