package com.tuc.masters.implementations.metrics

import com.tuc.masters.core.MetricCalculator
import com.tuc.masters.core.models.*
import org.springframework.stereotype.Component


@Component
class NumberOfUserActionsMetricCalculator : MetricCalculator {
    override var metricsDescription: MetricDescription
        get() = MetricDescription(
            "Average number of user actions",
            "Calculate (average) number of user actions in test (tests) \n" +
                    "Actions: clicks, keyboard interaction, scrolls, etc.",
            listOf(MetricLevel.GROUP, MetricLevel.SINGLE_TEST),
            listOf(ArtifactType.LOG_FILE),
        )
        set(_) {}

    override fun getSingleTestMetric(testParsedData: ParsedData, logsParsedData: ParsedData?): MetricResult {
        val result =
            (logsParsedData?.actions ?: listOf()).count {
                it.type in listOf(
                    ActionType.SCROLL,
                    ActionType.CLICK,
                    ActionType.SEND_KEYS
                )
            }
                .toDouble()
        return wrapResult(result)
    }
}