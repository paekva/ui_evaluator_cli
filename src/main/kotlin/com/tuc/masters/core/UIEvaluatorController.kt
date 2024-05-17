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
    @Autowired private val visualisers: List<Visualiser>,
    @Autowired private val metrics: List<ComplexityMetric>,
) {
    private var log: Logger = Logger.getLogger(UIEvaluatorCommand::class.java.getName())

    fun evaluate(projectPath: String) {
        val config = retrieveConfig(projectPath) ?: return

        val tests = findTestFiles(projectPath, config) ?: return
        val logs = findLogFiles(projectPath, config) ?: return

        val testData = handleParsing(config, logs, tests)
        val results = calculateSingleTestMetrics(testData, config)

        for (v in visualisers) {
            v.visualize(results)
        }
    }

    private fun findTestFiles(projectPath: String, config: EvaluatorConfig): List<File>? {
        val testsPath = projectPath + config.testsPath
        val testFileRegex = Regex("[a-zA-Z0-9]*" + config.testFilePostfix + "." + config.testExtension + "$")
        val tests = service.getFiles(
            testsPath,
            testFileRegex,
            config.exclude ?: listOf(),
        )

        if (tests.isEmpty()) {
            log.warning("No test files were found in provided folder.\ntests: $testsPath")
            return null
        }

        return tests
    }

    private fun findLogFiles(projectPath: String, config: EvaluatorConfig): List<File>? {
        val logsPath = projectPath + config.logsPath
        val logsFileRegex = Regex("[a-zA-Z0-9_]*\\." + config.logExtension + "$")
        val logs = service.getFiles(
            logsPath,
            logsFileRegex,
            listOf()
        )

        if (logs.isEmpty()) {
            log.warning("No log files were found in provided folder.\nlogs: $logsPath")
            return null
        }

        return logs
    }

    private fun retrieveConfig(projectPath: String): EvaluatorConfig? {
        // get config file in project root
        val configFile = File("$projectPath/config.yaml")
        if (!configFile.exists()) {
            log.warning("No configuration file was found in the root of the project.\nproject path: $projectPath")
            return null
        }
        val config: EvaluatorConfig?

        try {
            config = service.parseConfig(configFile)
        } catch (e: Exception) {
            log.warning("Config is malformed")
            return null
        }

        return config
    }

    private fun devMissingLogsFinder(testData: List<TestData>) {
        val tmp = File("./progress_zimbra.txt") // NAME
        if (!tmp.exists()) tmp.createNewFile()

        var res = ""
        testData.forEach {
            res += "${it.testName}: ${it.logs.isNotEmpty()}\n"
        }

        tmp.writeText(res)
        println("logs are available for ${testData.count { it.logs.isNotEmpty() }}/${testData.count()} tests")
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

    private fun calculateSingleTestMetrics(
        testData: List<TestData>,
        configFile: EvaluatorConfig
    ): Map<TestData, List<MetricResult>> {
        // calculate
        val logMetrics = metrics.filter { it.metricsDescription.artifactTypes.contains(ArtifactType.LOG_FILE) }
        val testMetrics = metrics.filter { it.metricsDescription.artifactTypes.contains(ArtifactType.TEST_SOURCE_CODE) }

        val results = mutableMapOf<TestData, List<MetricResult>>()
        testData.forEach {
            val logResults = service.calculateMetrics(it.logs, logMetrics)
            val testResults = service.calculateMetrics(it.sourceCode, testMetrics)
            results[it] = logResults + testResults
        }

        return results

    }
}