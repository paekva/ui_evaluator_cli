package com.tuc.masters.implementations.metrics

import com.tuc.masters.core.MetricCalculator
import com.tuc.masters.core.models.*
import org.springframework.stereotype.Component


@Component
class NumberOfAssertStatementsMetricCalculator : MetricCalculator {
    override fun getMetricDescription(): MetricDescription {
        return MetricDescription(
            "Average number of assert statements",
            "Calculate (average) number of assert statements in test (tests)",
            listOf(MetricLevel.GROUP, MetricLevel.SINGLE_TEST),
            listOf(ArtifactType.TEST_SOURCE_CODE),
        )
    }

    override fun getSingleTestMetric(testParsedData: ParsedData, logsParsedData: ParsedData?): MetricResult {
        val result = testParsedData.actions.count { it.type == ActionType.ASSERT }.toDouble()
        return wrapResult(result)
    }
}