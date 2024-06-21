package vsr.tuc.masters.core

import vsr.tuc.masters.core.models.*

// Describes the functionality that a metric calculation class should have
interface MetricCalculator {
    fun getMetricDescription(): MetricDescription

    fun wrapResult(result: Double, level: MetricLevel = MetricLevel.SINGLE_TEST): MetricResult {
        return MetricResult(
            this.getMetricDescription(),
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
