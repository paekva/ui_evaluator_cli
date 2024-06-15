package com.tuc.masters.core

import com.tuc.masters.commands.UIEvaluatorCommand
import com.tuc.masters.core.models.*
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.File
import java.util.logging.Logger

@Component
class UIEvaluatorController(
    @Autowired private val testMapper: TestMapper,
    @Autowired private val service: UIEvaluatorService,
    @Autowired private val logParsers: List<LogParser>,
    @Autowired private val testParsers: List<TestParser>,
    @Autowired private val visualisers: List<Visualiser>,
    @Autowired private val metrics: List<MetricCalculator>,
) {
    companion object : KLogging()

    fun evaluate(projectPath: String) {
        val config = testMapper.getMappingFromConfig(projectPath) ?: return

        val tests = findTestFiles(projectPath, config) ?: return
        val logs = findLogFiles(projectPath, config) ?: return

        var testData = handleParsing(config, logs, tests)

        showMissingLogs(projectPath, config, testData)

        if (config.skipTestsWithoutLogs) {
            testData = testData.filter { it.logs != null }
        }

        val singleMetricsResults = service.calculateMetrics(testData, metrics)
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
                    (e.key.testName.contains(it) || (e.key.filePath?.contains(it)
                        ?: false)) && !selectedTests.map { it.testName }.contains(e.key.testName)
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
        val tests = service.getFiles(testsPath, testFileRegex)

        if (tests.isEmpty()) {
            logger.warn {"No test files were found in provided folder.\ntests: $testsPath"}
            return null
        }

        return tests
    }

    private fun findLogFiles(projectPath: String, config: EvaluatorConfig): List<File>? {
        if (config.logsPath == null) return listOf()

        val logsPath = projectPath + config.logsPath
        val logsFileRegex = Regex("[a-zA-Z0-9_]*\\." + config.logExtension + "$")
        val logs = service.getFiles(logsPath, logsFileRegex)

        if (logs.isEmpty()) {
            logger.warn {"No log files were found in provided folder.\nlogs: $logsPath"}
            return null
        }

        return logs
    }

    private fun showMissingLogs(projectPath: String, config: EvaluatorConfig, testData: List<TestData>) {
        var res = ""
        val progressFile = File("$projectPath/ui_evaluator_progress.csv")
        if (!progressFile.exists()) progressFile.createNewFile()

        for (g in (config.groups?.toList() ?: listOf())) {
            res += "${g.first}\n"
            g.second.forEach {
                val suited = testData.filter { e ->
                    e.testName.contains(it) || (e.filePath?.contains(it) ?: false)
                }
                suited.forEach {
                    res += "${it.testName},${it.logs != null},${it.filePath}\n"
                }
            }
        }

        progressFile.writeText(res)
    }

    private fun handleParsing(config: EvaluatorConfig, logs: List<File>, tests: List<File>): List<TestData> {
        // parse logs
        val logParser = service.findLogParser(config, logParsers)
        val parsedLogsData = service.parseLogs(logs, config, logParser)

        // parse tests
        val testParser = service.findTestParser(config, testParsers)
        var parsedTestData = service.parseTests(tests, config, testParser)

        if (!config.exclude.isNullOrEmpty()) {
            config.exclude.forEach {
                parsedTestData =
                    parsedTestData.filter { t -> !(t.testName.contains(it) || (t.filePath ?: "").contains(it)) }
            }
        }
        if (!config.groups.isNullOrEmpty()) {
            parsedTestData =
                parsedTestData.filter { t ->
                    config.groups.values.flatten().any { t.testName == it || (t.filePath ?: "").contains(it) }
                }
        }

        return service.getTestData(parsedLogsData, parsedTestData)
    }
}