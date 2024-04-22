package com.tuc.masters.commands

import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption
import java.lang.String.format
import java.util.logging.Logger

@ShellComponent
class TmpCommand {
    var log: Logger = Logger.getLogger(TmpCommand::class.java.getName())
    @ShellMethod(value = "connect to remote server")
    fun ssh(@ShellOption(value = ["-s"]) remoteServer: String?) {
        log.info(format("Logged to machine '%s'", remoteServer))
    }
}