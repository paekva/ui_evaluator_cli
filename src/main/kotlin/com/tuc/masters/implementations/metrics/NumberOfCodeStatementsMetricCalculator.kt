package com.tuc.masters.implementations.metrics

import com.tuc.masters.core.MetricCalculator
import com.tuc.masters.core.models.*
import org.springframework.stereotype.Component

@Component
class NumberOfCodeStatementsMetricCalculator : MetricCalculator {
    override var metricsDescription: MetricDescription
        get() = MetricDescription(
            "Number of code statements in test's code",
            "Calculate number of code statements",
            listOf(MetricLevel.GROUP, MetricLevel.SINGLE_TEST),
            listOf(ArtifactType.TEST_SOURCE_CODE),
        )
        set(_) {}

    override fun calculateSingleTestMetric(parsedData: ParsedData): Double {
        return parsedData.actions.count().toDouble()
    }
}