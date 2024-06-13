package com.tuc.masters.implementations.metrics

import com.tuc.masters.core.MetricCalculator
import com.tuc.masters.core.models.*
import org.springframework.stereotype.Component


@Component
class TestExecutionTimeMetricCalculator : MetricCalculator {
    override var metricsDescription: MetricDescription
        get() = MetricDescription(
            "Average test execution time",
            "Calculate (average) test (tests) execution time" +
                    "Time it takes to perform the test based on the log data",
            listOf(MetricLevel.GROUP, MetricLevel.SINGLE_TEST),
            listOf(ArtifactType.LOG_FILE),
        )
        set(_) {}

    override fun getSingleTestMetric(testParsedData: ParsedData, logsParsedData: ParsedData?): MetricResult {
        val start =
            (logsParsedData?.actions ?: listOf()).firstOrNull { it.type == ActionType.START }?.timestamp?.toDouble()
                ?: 0.0
        val stop =
            (logsParsedData?.actions ?: listOf()).firstOrNull { it.type == ActionType.STOP }?.timestamp?.toDouble()
                ?: 0.0

        val result = if (stop == 0.0 || start == 0.0) 0.0 else stop - start
        return wrapResult(result)
    }

    override fun getGroupTestMetric(results: List<MetricResult>): MetricResult {
        return super.getGroupTestMetric(results.filter { it.value != 0.0 })
    }
}