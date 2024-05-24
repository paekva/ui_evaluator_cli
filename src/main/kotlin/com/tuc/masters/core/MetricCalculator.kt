package com.tuc.masters.core

import com.tuc.masters.core.models.*

interface MetricCalculator {
    var metricsDescription: MetricDescription
    fun calculateSingleTestMetric(actions: List<InterfaceAction>): Double

    // Basic group test metric is an average of calculated values
    fun calculateGroupTestMetric(results: List<MetricResult>): Double {
        return results.sumOf { it.value } / results.count()
    }

    fun getSingleTestMetric(parsedData: List<InterfaceAction>): MetricResult {
        return MetricResult(
            this.metricsDescription,
            calculateSingleTestMetric(parsedData),
            MetricLevel.SINGLE_TEST,
        )
    }

    fun getGroupTestMetric(result: List<MetricResult>): MetricResult {
        return MetricResult(
            metricsDescription,
            calculateGroupTestMetric(result),
            MetricLevel.GROUP,
        )
    }
}
