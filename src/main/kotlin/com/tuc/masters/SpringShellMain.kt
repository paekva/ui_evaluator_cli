package com.tuc.masters

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

// TODO:
//  1. Implement parser for test source code
//  2. Configuration and test mapper
@SpringBootApplication
open class SpringShellMain {
  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      SpringApplication.run(SpringShellMain::class.java, *args)
    }
  }
}

// TODO: how we deal with
//  - multiple implementations of the parsers exist for the same technologies
//  - should we separate logs and test parsers implementations