package com.tuc.masters.core

import com.tuc.masters.core.models.ArtifactType
import com.tuc.masters.core.models.EvaluatorConfig
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

    private fun getTestFiles(path: String, matchFileName: Regex, excludeNames: List<String>): List<File>? {
        val repoDir = File(path)
        return repoDir.listFiles()?.filter { matchFileName.matches(it.name) && !excludeNames.contains(it.name) }
    }
    private fun getLogFiles(path: String, matchFileName: Regex, excludeNames: List<String>): List<File>? {
        val repoDir = File(path)
        return repoDir.listFiles()?.filter { matchFileName.matches(it.name) && !excludeNames.contains(it.name) }
    }

    fun evaluate(configPath: String) {
        val configFile = File(configPath)
        val config = service.parseConfig(configFile)

        val tests = getTestFiles(
            config.path + config.testsPath,
            Regex("[a-zA-Z0-9]*" + config.testFilePostfix + "." + config.testExtension + "$"),
            config.exclude ?: listOf(),
        ) ?: listOf()

        val logs = getLogFiles(
            config.path + config.logsPath,
            Regex("[a-zA-Z0-9]*\\." + config.logExtension + "$"),
            listOf()
            ) ?: listOf()

        evaluateTestList(config, logs, tests)
    }

    private fun evaluateTestList(configFile: EvaluatorConfig, logs: List<File>, tests: List<File>){
        // parse logs
        val logParser = service.findLogParser(configFile, logParsers)
        val parsedLogsData = service.parseLogs(logs, configFile, logParser)

        // parse tests
        val testParser = service.findTestParser(configFile, testParsers)
        val parsedTestData = service.parseTests(tests, configFile, testParser)

        // calculate
        val logMetrics = metrics.filter { it.metricsDescription.artifactTypes.contains(ArtifactType.LOG_FILE) }
        val result1 = service.calculateMetrics(parsedLogsData, logMetrics)

        val testMetrics = metrics.filter { it.metricsDescription.artifactTypes.contains(ArtifactType.TEST_SOURCE_CODE) }
        val result2 = service.calculateMetrics(parsedTestData, testMetrics)

        // visualise
        result1.entries.forEach {
            println("\n\nfor test ${it.key.testName}")
            it.value.forEach { m ->
                println("${m.metric.name}: ${m.value}")
            }
        }

        // visualise
        result2.entries.forEach {
            println("\n\nfor test ${it.key.testName}")
            it.value.forEach { m ->
                println("${m.metric.name}: ${m.value}")
            }
        }
    }
}