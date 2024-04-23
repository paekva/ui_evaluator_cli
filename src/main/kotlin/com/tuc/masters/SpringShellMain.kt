package com.tuc.masters

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

// TODO:
//  1. Implement parser for logs (make sure to check it works for both PHP and Java logs)
//  2. Implement parser for test source code
//  3. Create controller module that handles workflow
//  4. Implement workflow for a single test and a single metric (+ add visualisation module)
//  5. Try to write Evaluation draft
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