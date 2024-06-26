package vsr.tuc.masters.core

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import vsr.tuc.masters.core.models.EvaluatorConfig
import mu.KLogging
import org.springframework.stereotype.Component
import java.io.File
import java.lang.Exception

// Component that handles the extraction of data from the config
@Component
class TestMapper {
    companion object : KLogging()

    fun getMappingFromConfig(path: String): EvaluatorConfig? {
        val configFile = File("$path/ui_evaluator_config.yaml")
        if (!configFile.exists()) {
            logger.warn {
                "No configuration file ui_evaluator_config.yaml was found in the selected path" +
                        "\nPath: $path"
            }
            return null
        }
        val config: EvaluatorConfig?

        try {
            config = parseConfig(configFile, path)
        } catch (e: Exception) {
            logger.warn { "Config is malformed" }
            return null
        }

        return config
    }

    private fun parseConfig(file: File, projectPath: String): EvaluatorConfig {
        val jackson = ObjectMapper(YAMLFactory()).registerKotlinModule()
        val tmp = jackson.readValue(file.bufferedReader(), EvaluatorConfig::class.java)
            ?: EvaluatorConfig(testsPath = "./tests", testFilePostfix = "")

        return tmp.copy(projectPath = tmp.projectPath ?: projectPath)
    }
}