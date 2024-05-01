package com.tuc.masters.core

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.tuc.masters.core.models.*
import org.springframework.stereotype.Component
import java.io.File

@Component
class UIEvaluatorService {

    fun parseConfig(file: File): EvaluatorConfig {
        val jackson = ObjectMapper(YAMLFactory()).registerKotlinModule()
        return jackson.readValue(file.bufferedReader(), EvaluatorConfig::class.java)
            ?: EvaluatorConfig(testsPath = "./tests", testFilePostfix = "")
    }

    fun getFiles(path: String, matchFileName: Regex, excludeNames: List<String>): List<File>? {
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
            testData.add(TestData(test.testName, test.actions, log?.actions ?: listOf()))
        }

        return testData
    }

    fun findLogParser(config: EvaluatorConfig, parsers: List<LogParser>): LogParser {
        return parsers[0]
    }

    fun findTestParser(config: EvaluatorConfig, parsers: List<TestParser>): TestParser {
        return parsers[0]
    }

    fun parseLogs(files: List<File>, config: EvaluatorConfig, parser: LogParser): List<ParsedData> {
        val parsedData = mutableListOf<ParsedData>()
        files.forEach { log ->
            val result = parser.parseFile(log, config)
            parsedData.add(ParsedData(log.name.split(".log")[0], result))
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