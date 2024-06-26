package vsr.tuc.masters

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

// Entry class for the application
@SpringBootApplication
open class SpringShellMain {
  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      SpringApplication.run(SpringShellMain::class.java, *args)
    }
  }
}