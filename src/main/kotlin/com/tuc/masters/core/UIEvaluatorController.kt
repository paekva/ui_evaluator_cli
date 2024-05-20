package com.tuc.masters.core

import com.tuc.masters.UIEvaluatorCommand
import com.tuc.masters.core.models.*
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
        val singleMetricsResults = calculateSingleTestMetrics(testData)
        val groupMetricsResults = calculateGroupMetrics(singleMetricsResults, config)
        for (v in visualisers) {
            v.visualizeSingleMetrics(config, singleMetricsResults)
            v.visualizeGroupMetrics(config, groupMetricsResults)
        }
    }

    private fun calculateGroupMetrics(
        results: Map<TestData, List<MetricResult>>,
        config: EvaluatorConfig
    ): Map<GroupData, List<MetricResult>> {
        val groupResults = mutableMapOf<GroupData, List<MetricResult>>()
        for (g in config.groups?.toList() ?: listOf()) {
            val gR = mutableListOf<MetricResult>()
            val testData = results.entries.filter { g.second.contains(it.key.testName) }
            val data = testData.map { it.value }
            for (m in metrics) {
                val tests = data.map { it.filter { tm -> tm.metric.name == m.metricsDescription.name } }
                    .reduce { acc, metricResults -> acc + metricResults }
                gR.add(m.getGroupTestMetric(tests))
            }
            groupResults[GroupData(groupName = g.first, tests = testData.map { it.key }.toList())] = gR
        }
        return groupResults
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
        if (config.logsPath == null) return listOf()

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
            config = service.parseConfig(configFile, projectPath)
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
        val logParser = service.findLogParser(logParsers)
        val parsedLogsData = service.parseLogs(logs, configFile, logParser)

        // parse tests
        val testParser = service.findTestParser(configFile, testParsers)
        val parsedTestData = service.parseTests(tests, configFile, testParser)

        return service.getTestData(parsedLogsData, parsedTestData)
    }

    private fun calculateSingleTestMetrics(testData: List<TestData>): Map<TestData, List<MetricResult>> {
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