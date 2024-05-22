package com.tuc.masters

import com.tuc.masters.core.UIEvaluatorController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption
import java.util.logging.Logger

@ShellComponent
class UIEvaluatorCommand(@Autowired private val controller: UIEvaluatorController) {
    private var log: Logger = Logger.getLogger(UIEvaluatorCommand::class.java.getName())

    @ShellMethod(value = "Evaluates application based on provided UI tests and config")
    fun evaluate(@ShellOption(defaultValue = "./") path: String) {
//        try {
            controller.evaluate(path)
//        } catch (e: Exception) {
//            log.severe("Something went wrong during the process: $e")
//        }
    }
}