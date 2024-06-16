package com.tuc.masters.core

import com.tuc.masters.core.models.*
import org.springframework.stereotype.Component
import java.io.File
import java.util.*

// Service handles additional logic for managing the calcualtion process
@Component
class UIEvaluatorService {

    fun getFiles(path: String, matchFileName: Regex): List<File> {
        val files = mutableListOf<File>()
        val repoDir = File(path)
        repoDir.listFiles()?.forEach { file ->
            files.addAll(getFilesFromDirectory(file))
        }

        return files.filter { matchFileName.matches(it.name) }
    }

    private fun getFilesFromDirectory(file: File): List<File> {
        if (file.isFile) {
            return listOf(file)
        } else if (file.isDirectory) {
            return file.listFiles()?.map { getFilesFromDirectory(it) }?.flatten() ?: listOf()
        }
        return listOf()
    }


    fun getTestData(logs: List<ParsedData>, tests: List<ParsedData>): List<TestData> {
        val testData = mutableListOf<TestData>()

        tests.forEach { test ->
            val logData = findLogByTest(test, logs)
            testData.add(TestData(test.testName, test.filePath, test, logData))
        }

        return testData
    }

    private fun findLogByTest(test: ParsedData, logs: List<ParsedData>): ParsedData? {
        val fullTestPath = (test.filePath ?: "tests").split("tests")[1].split("/")
            .filter { k -> k.isNotEmpty() }
        val testPath = fullTestPath.dropLast(1).joinToString("/")
        val testFileName = fullTestPath.last().split(".").first()

        val logData = logs.find {
            val lPath = (it.filePath ?: "logs").split("logs")[1].split("/")
                .filter { k -> k.isNotEmpty() }.dropLast(1).joinToString("/")
            it.testName == test.testName && (lPath == testPath || lPath == "${testPath}/${testFileName}")
        }

        return logData
    }

    fun findLogParser(config: EvaluatorConfig, parsers: List<LogParser>): LogParser {
        val browser = config.testExtension.toString()
        return parsers.find { containedInListCaseInsensitive(browser, it.supportedBrowsers) }
            ?: parsers[0]
    }

    fun findTestParser(config: EvaluatorConfig, parsers: List<TestParser>): TestParser {
        val lan = config.testLanguage.toString()
        val framework = config.testFramework.toString()
        return parsers.find {
            containedInListCaseInsensitive(
                lan,
                it.supportedLanguages
            ) && containedInListCaseInsensitive(framework, it.supportedFrameworks)
        }
            ?: parsers[0]
    }

    private fun containedInListCaseInsensitive(item: String, list: List<String>): Boolean {
        val el = item.lowercase(Locale.getDefault())
        val elements = list.map { l -> l.lowercase(Locale.getDefault()) }
        return elements.contains(el)
    }

    fun parseLogs(files: List<File>, config: EvaluatorConfig, parser: LogParser): List<ParsedData> {
        val parsedData = mutableListOf<ParsedData>()
        files.forEach { logData ->
            val result = parser.parseFile(logData, config)
            parsedData.add(result)
        }

        return parsedData
    }

    fun parseTests(files: List<File>, config: EvaluatorConfig, parser: TestParser): List<ParsedData> {
        val parsedData = mutableListOf<ParsedData>()

        files.forEach { test ->
            val result = parser.parseFile(test, config)
            parsedData.addAll(result)
        }

        return parsedData
    }

    fun calculateMetrics(testData: List<TestData>, metrics: List<MetricCalculator>): Map<TestData, List<MetricResult>> {
        val results = mutableMapOf<TestData, List<MetricResult>>()
        testData.forEach {
            results[it] = metrics.map { metric -> metric.getSingleTestMetric(it.sourceCode, it.logs) }.toList()
        }

        return results
    }
}