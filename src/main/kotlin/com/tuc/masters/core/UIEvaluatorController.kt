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

        var testData = handleParsing(config, logs, tests)
        showMissingLogs(projectPath, config, testData)

        if (config.skipTestsWithoutLogs) {
            testData = testData.filter { it.logs.isNotEmpty() }
        }
        val singleMetricsResults = calculateSingleTestMetrics(testData)
        val groupMetricsResults = calculateGroupMetrics(singleMetricsResults, config)
        for (v in visualisers) {
            v.visualizeSingleMetrics(config, singleMetricsResults)
            v.visualizeGroupMetrics(config, groupMetricsResults)
        }
    }

    private fun calculateGroupMetrics(
        singleResults: Map<TestData, List<MetricResult>>,
        config: EvaluatorConfig
    ): Map<GroupData, List<MetricResult>> {
        if (config.groups.isNullOrEmpty()) return mapOf()

        val groupResults = mutableMapOf<GroupData, List<MetricResult>>()
        for (g in config.groups.toList()) {
            val groupMetrics = mutableListOf<MetricResult>()
            val selected = arrayListOf<List<MetricResult>>()
            val selectedTests = arrayListOf<TestData>()
            g.second.forEach {
                val suited = singleResults.entries.filter { e ->
                    e.key.testName.contains(it) || (e.key.filePath?.contains(it) ?: false)
                }
                selectedTests.addAll(suited.map { it.key })
                selected.addAll(suited.map { it.value })
            }

            for (m in metrics) {
                val tests = selected.map { it.filter { tm -> tm.metric.name == m.metricsDescription.name } }
                if (tests.isNotEmpty()) {
                    val tmp = tests.reduce { acc, metricResults -> acc + metricResults }
                    groupMetrics.add(m.getGroupTestMetric(tmp))
                }
            }
            groupResults[GroupData(groupName = g.first, tests = selectedTests)] = groupMetrics
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
        val configFile = File("$projectPath/ui_evaluator_config.yaml")
        if (!configFile.exists()) {
            log.warning(
                "No configuration file ui_evaluator_config.yaml was found in the root of the project." +
                        "\nProject path: $projectPath"
            )
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

    private fun showMissingLogs(projectPath: String, config: EvaluatorConfig, testData: List<TestData>) {
        var res = ""
        val tmp = File("$projectPath/progress.csv") // NAME
        if (!tmp.exists()) tmp.createNewFile()

        for (g in (config.groups?.toList() ?: listOf())) {
            res += "${g.first}\n"
            g.second.forEach {
                val suited = testData.filter { e ->
                    e.testName.contains(it) || (e.filePath?.contains(it) ?: false)
                }
                suited.forEach {
                    res += "${it.testName},${it.logs.isNotEmpty()},${it.filePath}\n"
                }
            }
        }

        tmp.writeText(res)
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