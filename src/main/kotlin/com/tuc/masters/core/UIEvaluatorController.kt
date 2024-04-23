package com.tuc.masters.core

import com.tuc.masters.core.models.ActionType
import com.tuc.masters.core.models.ArtifactType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.File

@Component
class UIEvaluatorController(
    @Autowired private val service: UIEvaluatorService,
    @Autowired private val parsers: List<LogParser>,
    @Autowired private val metrics: List<ComplexityMetric>,
) {

    fun evaluate(path: String) {
        // TODO: implement proper config class to be parsed from file and used
        val configFile = File("")
        val testDataPath = service.extractConfig(configFile)

        // getting artifacts
        val dir = File("${path}/${testDataPath}")
        val logs = service.getAvailableLogs(dir)
//        val tests = service.getAvailableTests(dir) TODO

        // parse logs
        val parser = service.findLogParser(configFile, parsers)
        val parsedData = service.parseLogs(logs, parser)

        // parse tests
        // TODO

        // calculate
        val logMetrics = metrics.filter { it.metricsDescription.artifactTypes.contains(ArtifactType.LOG_FILE) }
        val result = service.calculateMetrics(parsedData, logMetrics)

        // visualise
        result.entries.forEach {
            println("\n\nfor test ${it.key.testName}")
            it.value.forEach { m ->
                println("${m.metric.name}: ${m.value}")
            }
        }
    }
}