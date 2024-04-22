package com.tuc.masters.commands

import com.tuc.masters.metrics.ComplexityMetric
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption
import java.lang.String.format
import java.util.logging.Logger

@ShellComponent
class TmpCommand(
    @Autowired private val listOfImpls: List<ComplexityMetric>
) {
    private var log: Logger = Logger.getLogger(TmpCommand::class.java.getName())
    @ShellMethod(value = "bla bla")
    fun metrics() {
        log.info(format("Number of implementations found: '%s'", listOfImpls.size))
    }
}