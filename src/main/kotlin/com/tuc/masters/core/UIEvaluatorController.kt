package com.tuc.masters.core

import com.tuc.masters.core.models.ArtifactType
import com.tuc.masters.core.models.MetricResult
import com.tuc.masters.core.models.TestData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.File

@Component
class UIEvaluatorController(
    @Autowired private val service: UIEvaluatorService,
    @Autowired private val logParsers: List<LogParser>,
    @Autowired private val testParsers: List<TestParser>,
    @Autowired private val metrics: List<ComplexityMetric>,
) {

    fun evaluate(path: String) {
        // TODO: implement proper config class to be parsed from file and used
        val configFile = File("")
        val testDataPath = service.extractConfig(configFile)

        // getting artifacts
        val dir = File("${path}/${testDataPath}")
        // TODO: add white or black listing for files
        //  also mapping module!
        val logs = service.getFilesByExtension(dir, "log") // TODO: should be taken from the config
        val tests = service.getFilesByExtension(dir, "java") // TODO: should be taken from the config

        // parse logs
        val logParser = service.findLogParser(configFile, logParsers)
        val parsedLogsData = service.parseLogs(logs, logParser)

        // parse tests
        val testParser = service.findTestParser(configFile, testParsers)
        val parsedTestData = service.parseTests(tests, testParser)

        // calculate
        val logMetrics = metrics.filter { it.metricsDescription.artifactTypes.contains(ArtifactType.LOG_FILE) }
        val result1 = service.calculateMetrics(parsedLogsData, logMetrics)

        val testMetrics = metrics.filter { it.metricsDescription.artifactTypes.contains(ArtifactType.TEST_SOURCE_CODE) }
        val result2 = service.calculateMetrics(parsedTestData, testMetrics)

        val result = mutableMapOf<TestData, List<MetricResult>>()
        result2.forEach{
            result[it.key] = (result1[it.key] ?: listOf()) + it.value
        }

        // visualise
        result1.entries.forEach {
            println("\n\nfor test ${it.key.testName}")
            it.value.forEach { m ->
                println("${m.metric.name}: ${m.value}")
            }
        }
    }
}