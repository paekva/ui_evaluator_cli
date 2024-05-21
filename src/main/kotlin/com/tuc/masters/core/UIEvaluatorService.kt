package com.tuc.masters.core

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.tuc.masters.core.models.*
import org.springframework.stereotype.Component
import java.io.File
import java.util.*

@Component
class UIEvaluatorService {

    fun parseConfig(file: File, projectPath: String): EvaluatorConfig {
        val jackson = ObjectMapper(YAMLFactory()).registerKotlinModule()
        val tmp = jackson.readValue(file.bufferedReader(), EvaluatorConfig::class.java)
            ?: EvaluatorConfig(testsPath = "./tests", testFilePostfix = "")

        return tmp.copy(projectPath = projectPath)
    }

    fun getFiles(path: String, matchFileName: Regex, excludeNames: List<String>): List<File> {
        val files = mutableListOf<File>()
        val repoDir = File(path)
        repoDir.listFiles()?.forEach { file ->
            files.addAll(getFilesFromDirectory(file))
        }

        return files.filter { matchFileName.matches(it.name) && !excludeNames.contains(it.name) }
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
            val log = logs.find { it.testName == test.testName }
            testData.add(TestData(test.testName, test.fileName, test.actions, log?.actions ?: listOf()))
        }

        return testData
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
        files.forEach { log ->
            val result = parser.parseFile(log, config)
            parsedData.add(ParsedData(log.name.split(".log")[0], null, result))
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

    fun calculateMetrics(
        parsedData: List<InterfaceAction>,
        metrics: List<ComplexityMetric>
    ): List<MetricResult> {
        return metrics.map { metric -> metric.getSingleTestMetric(parsedData) }.toList()
    }
}