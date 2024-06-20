package com.tuc.masters.implementations.metrics

import com.tuc.masters.core.MetricCalculator
import com.tuc.masters.core.models.*
import org.springframework.stereotype.Component

@Component
class NumberOfCodeLinesMetricCalculator : MetricCalculator {
    override fun getMetricDescription(): MetricDescription {
        return MetricDescription(
            "Average number of code lines",
            "Calculate (average) number of code lines in test (tests)",
            listOf(MetricLevel.GROUP, MetricLevel.SINGLE_TEST),
            listOf(ArtifactType.TEST_SOURCE_CODE),
        )
    }

    override fun getSingleTestMetric(testParsedData: ParsedData, logsParsedData: ParsedData?): MetricResult {
        val result = testParsedData.rawData.split("\n").count().toDouble()
        return wrapResult(result)
    }
}