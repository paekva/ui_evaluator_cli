package vsr.tuc.masters.core

import vsr.tuc.masters.core.models.*
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.File

// The controller handles all logic for the calculation of metrics
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

    fun info() {
        logger.info { "" }
        logger.info { "------------------------------------------------------------------------------------------------------------------" }
        logger.info { "This is the UI Tests Evaluator CLI app, that calculates the complexity of the interface by analysing the UI Tests" }
        logger.info { "Use `evaluate` --path <config_path> command to start the evaluation process" }
        logger.info { "The example of the config can be found in the repository at https://github.com/paekva/ui_evaluator_cli" }
        logger.info { "Please, refer to the README.md file to get more information" }
        logger.info { "------------------------------------------------------------------------------------------------------------------" }
        logger.info { "" }
    }

    fun evaluate(path: String, debug: Boolean) {
        if (debug) {
            logger.info { "Fetching config" }
        }
        val config = testMapper.getMappingFromConfig(path) ?: return

        if (debug) {
            logger.info { "Fetching test files" }
        }
        val tests = findTestFiles(config) ?: return
        if (debug) {
            logger.info { "Found ${tests.count()} test files" }
            logger.info { "Fetching log files" }
        }
        val logs = findLogFiles(config) ?: return
        if (debug) {
            logger.info { "Found ${logs.count()} log files" }
            logger.info { "Parsing tests data" }
        }

        var testData = handleParsing(config, logs, tests, debug)
        if (debug) {
            logger.info { "Found ${testData.count()} tests" }
            logger.info { "Looking for missing logs data" }
        }

        service.findMissingLogs(config, testData)
        if (debug) {
            logger.info { "Skipping tests without logs if required by config" }
        }

        if (config.skipTestsWithoutLogs) {
            testData = testData.filter { it.logs != null }
        }
        if (debug) {
            logger.info { "Remaining tests: ${testData.count()}" }
            logger.info { "Calculating single metrics result" }
        }

        val singleMetricsResults = service.calculateMetrics(testData, metrics)
        if (debug) {
            logger.info { "Metrics calculated for ${singleMetricsResults.keys.count()} tests" }
            logger.info { "Calculating group metrics result" }
        }
        val groupMetricsResults = calculateGroupMetrics(singleMetricsResults, config)
        if (debug) {
            logger.info { "Metrics calculated for ${groupMetricsResults.keys.count()} groups" }
            logger.info { "Visualising results" }
        }

        for (v in visualisers) {
            if (config.visualiseSingleTestMetrics) v.visualizeSingleMetrics(config, singleMetricsResults)
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
                val fixed = if (it[0] == '/') it.drop(1) else if (it[it.count() - 1] == '/') it.dropLast(1) else it
                val suited = singleResults.entries.filter { e ->
                    (e.key.testName.contains(fixed) || (e.key.filePath?.contains(fixed)
                        ?: false)) && !selectedTests.map { it.testName }.contains(e.key.testName)
                }
                selectedTests.addAll(suited.map { s -> s.key })
                selected.addAll(suited.map { s -> s.value })
            }

            for (m in metrics) {
                val tests = selected.map { it.filter { tm -> tm.metric.name == m.getMetricDescription().name } }
                if (tests.isNotEmpty()) {
                    val tmp = tests.reduce { acc, metricResults -> acc + metricResults }
                    groupMetrics.add(m.getGroupTestMetric(tmp))
                }
            }
            groupResults[GroupData(groupName = g.first, tests = selectedTests)] = groupMetrics
        }
        return groupResults
    }

    private fun findTestFiles(config: EvaluatorConfig): List<File>? {
        val testsPath = config.projectPath + config.testsPath
        val testFileRegex = Regex("[a-zA-Z0-9]*" + config.testFilePostfix + "." + config.testExtension + "$")
        val tests = service.getFiles(testsPath, testFileRegex)

        if (tests.isEmpty()) {
            logger.warn { "No test files were found in provided folder.\ntests: $testsPath" }
            return null
        }

        return tests
    }

    private fun findLogFiles(config: EvaluatorConfig): List<File>? {
        if (config.logsPath == null) return listOf()

        val logsPath = config.projectPath + config.logsPath
        val logsFileRegex = Regex("[a-zA-Z0-9_]*\\." + config.logExtension + "$")
        val logs = service.getFiles(logsPath, logsFileRegex)

        if (logs.isEmpty()) {
            logger.warn { "No log files were found in provided folder.\nlogs: $logsPath" }
            return null
        }

        return logs
    }

    private fun handleParsing(
        config: EvaluatorConfig,
        logs: List<File>,
        tests: List<File>,
        debug: Boolean
    ): List<TestData> {
        // parse logs
        val logParser = service.findLogParser(config, logParsers)
        val parsedLogsData = service.parseLogs(logs, config, logParser)
        if (debug) {
            logger.info { "Parsed logs: ${parsedLogsData.count()}" }
        }
        // parse tests
        val testParser = service.findTestParser(config, testParsers)
        var parsedTestData = service.parseTests(tests, config, testParser)
        if (debug) {
            logger.info { "Parsed tests: ${parsedTestData.count()}" }
        }

        if (!config.exclude.isNullOrEmpty()) {
            config.exclude.forEach {
                val testFromConfig =
                    if (it[0] == '/') it.drop(1) else if (it[it.count() - 1] == '/') it.dropLast(1) else it
                parsedTestData =
                    parsedTestData.filter { t ->
                        !(t.testName.contains(testFromConfig) || (t.filePath ?: "").contains(
                            testFromConfig
                        ))
                    }
            }
        }
        if (debug) {
            logger.info { "After applying config exclusion - ${parsedTestData.count()} tests" }
        }
        if (!config.groups.isNullOrEmpty()) {
            parsedTestData =
                parsedTestData.filter { t ->
                    config.groups.values.flatten().any {
                        val testFromConfig =
                            if (it[0] == '/') it.drop(1) else if (it[it.count() - 1] == '/') it.dropLast(1) else it
                        t.testName == it || (t.filePath ?: "").contains(testFromConfig)
                    }
                }
        }
        if (debug) {
            logger.info { "After applying config grouping - ${parsedTestData.count()} tests" }
        }

        return service.getTestData(parsedLogsData, parsedTestData)
    }
}