package com.tuc.masters.core

import com.tuc.masters.core.models.ActionType
import com.tuc.masters.core.models.MetricResult
import com.tuc.masters.core.models.TestData
import org.springframework.stereotype.Component
import java.io.File

@Component
class UIEvaluatorService {

    fun extractConfig(file: File):String {
        return "./files"
    }

    fun getAvailableLogs(dir: File): List<File> {
        val files = dir.listFiles()
        return files.filter { it.isFile && it.extension.contains("log") }
    }

    fun findLogParser(config: File, parsers: List<LogParser>): LogParser {
        return parsers[0]
    }

    fun parseLogs(files: List<File>, parser: LogParser): List<TestData> {
        val testData = mutableListOf<TestData>()
        files.forEach { log ->
            val result = parser.parseFile(log)
            val tmp1 = result.filter { it.type == ActionType.CLICK }
            println("done for ${log.path} ${tmp1.count()}")
            testData.add(TestData(log.name, result))
        }

        return testData
    }

    fun calculateMetrics(parsedData: List<TestData>, metrics: List<ComplexityMetric>): Map<TestData, List<MetricResult>> {
        val results = mutableMapOf<TestData, List<MetricResult>>()
        parsedData.forEach{data ->
            results[data] = metrics.map { metric -> metric.getSingleTestMetric(data) }.toList()
        }
        return results
    }
}