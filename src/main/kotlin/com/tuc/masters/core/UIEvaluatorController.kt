package com.tuc.masters.core

import com.tuc.masters.UIEvaluatorCommand
import com.tuc.masters.core.models.ArtifactType
import com.tuc.masters.core.models.EvaluatorConfig
import com.tuc.masters.core.models.MetricResult
import com.tuc.masters.core.models.TestData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.File
import java.lang.Exception
import java.util.logging.Logger

@Component
class UIEvaluatorController(
    @Autowired private val service: UIEvaluatorService,
    @Autowired private val logParsers: List<LogParser>,
    @Autowired private val testParsers: List<TestParser>,
    @Autowired private val metrics: List<ComplexityMetric>,
) {
    private var log: Logger = Logger.getLogger(UIEvaluatorCommand::class.java.getName())

    fun evaluate(projectPath: String) {
        // get config file in project root
        val configFile = File("$projectPath/config.yaml")
        if(!configFile.exists()) {
            log.warning("No configuration file was found in the root of the project.\nproject path: $projectPath")
            return
        }
        val config: EvaluatorConfig?

        try {
            config = service.parseConfig(configFile)
        } catch (e: Exception) {
            log.warning("Config is malformed")
            return
        }

        // find all test and log files
        val testsPath = projectPath + config.testsPath
        val testFileRegex = Regex("[a-zA-Z0-9]*" + config.testFilePostfix + "." + config.testExtension + "$")
        val tests = service.getFiles(
            testsPath,
            testFileRegex,
            config.exclude ?: listOf(),
        ) ?: listOf()
        val logsPath = projectPath + config.logsPath
        val logsFileRegex = Regex("[a-zA-Z0-9_]*\\." + config.logExtension + "$")
        val logs = service.getFiles(
            logsPath,
            logsFileRegex,
            listOf()
        ) ?: listOf()

        if(tests.isEmpty() && logs.isEmpty()) {
            log.warning("No test or log files were found in provided folders.\nlogs: $logsPath\ntests: $testsPath")
            return
        }

        val testData = handleParsing(config, logs, tests)
        println("logs are available for ${testData.count { it.logs.isNotEmpty() }}/${testData.count()} tests")

        calculateSingleTestMetrics(testData, config)
    }

    private fun handleParsing(configFile: EvaluatorConfig, logs: List<File>, tests: List<File>): List<TestData> {
        // parse logs
        val logParser = service.findLogParser(configFile, logParsers)
        val parsedLogsData = service.parseLogs(logs, configFile, logParser)

        // parse tests
        val testParser = service.findTestParser(configFile, testParsers)
        val parsedTestData = service.parseTests(tests, configFile, testParser)

        return service.getTestData(parsedLogsData, parsedTestData)
    }

    private fun calculateSingleTestMetrics(testData: List<TestData>, configFile: EvaluatorConfig) {
        // calculate
        val logMetrics = metrics.filter { it.metricsDescription.artifactTypes.contains(ArtifactType.LOG_FILE) }
        val testMetrics = metrics.filter { it.metricsDescription.artifactTypes.contains(ArtifactType.TEST_SOURCE_CODE) }

        val results = mutableMapOf<TestData, List<MetricResult>>()
        testData.forEach {
            val logResults = service.calculateMetrics(it.logs, logMetrics)
            val testResults = service.calculateMetrics(it.sourceCode, testMetrics)
            results[it] = logResults + testResults
        }

        // visualise
        results.entries.forEach {
            println("\n\n-----------------------------------------------------")
            println("for test ${it.key.testName} (log are${if(it.key.logs.isEmpty()) " not " else " "}available)")
            println("-----------------------------------------------------")
            it.value.forEach { m ->
                println("${m.metric.name}: ${m.value}")
            }
        }
    }
}