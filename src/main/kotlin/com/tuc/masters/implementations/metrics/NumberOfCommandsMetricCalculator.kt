package com.tuc.masters.implementations.metrics

import com.tuc.masters.core.MetricCalculator
import com.tuc.masters.core.models.*
import org.springframework.stereotype.Component

@Component
class NumberOfCommandsMetricCalculator : MetricCalculator {
    override fun getMetricDescription(): MetricDescription {
        return MetricDescription(
            "Average number of commands",
            "Calculate (average) number of commands in test (tests)",
            listOf(MetricLevel.GROUP, MetricLevel.SINGLE_TEST),
            listOf(ArtifactType.TEST_SOURCE_CODE),
        )
    }

    override fun getSingleTestMetric(testParsedData: ParsedData, logsParsedData: ParsedData?): MetricResult {
        val result = testParsedData.actions.count().toDouble()
        return wrapResult(result)
    }
}