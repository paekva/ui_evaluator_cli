package com.tuc.masters.metrics

import com.tuc.masters.metrics.models.*

interface ComplexityMetric {
    var metricsDescription: MetricDescription
    fun calculateSingleTestMetric(actions: List<InterfaceAction>): Double

    // Basic group test metric is an average of calculated values
    fun calculateGroupTestMetric(results: List<MetricResult>): Double {
        return results.sumOf { it.value } / results.count()
    }
    fun getSingleTestMetric(testData: TestData): MetricResult{
        return MetricResult(
            this.metricsDescription,
            calculateSingleTestMetric(testData.actions),
            MetricLevel.SINGLE_TEST,
            listOf(testData)
        )
    }

    fun getGroupTestMetric(result: List<MetricResult>): MetricResult {
        return MetricResult(
            metricsDescription,
            calculateGroupTestMetric(result),
            MetricLevel.GROUP,
            result.map { it.tests[0] }.toList()
        )
    }
}

// TODO(me): mb we need a custom exception that can be thrown by the method to indicate
//  that there is not enough data to calculate metric?? is this needed?