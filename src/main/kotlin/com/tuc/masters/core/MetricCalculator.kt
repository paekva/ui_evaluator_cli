package com.tuc.masters.core

import com.tuc.masters.core.models.*

// Describes the functionality that a metric calculation class should have
interface MetricCalculator {
    var metricsDescription: MetricDescription

    fun wrapResult(result: Double, level: MetricLevel = MetricLevel.SINGLE_TEST): MetricResult {
        return MetricResult(
            this.metricsDescription,
            result,
            level,
        )
    }

    fun getSingleTestMetric(testParsedData: ParsedData, logsParsedData: ParsedData?): MetricResult

    fun getGroupTestMetric(results: List<MetricResult>): MetricResult {
        val result = results.sumOf { it.value } / results.count()

        return wrapResult(result, MetricLevel.GROUP)
    }
}
