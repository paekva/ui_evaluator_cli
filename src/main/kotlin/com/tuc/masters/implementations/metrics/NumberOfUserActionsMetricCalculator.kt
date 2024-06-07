package com.tuc.masters.implementations.metrics

import com.tuc.masters.core.MetricCalculator
import com.tuc.masters.core.models.*
import org.springframework.stereotype.Component


@Component
class NumberOfUserActionsMetricCalculator: MetricCalculator {
    override var metricsDescription: MetricDescription
        get() = MetricDescription(
            "Average number of user actions per test",
            "Calculate actions, such as clicks, keyboard interaction, scrolls, etc.",
            listOf(MetricLevel.GROUP, MetricLevel.SINGLE_TEST),
            listOf(ArtifactType.LOG_FILE),
        )
        set(_) {}

    override fun calculateSingleTestMetric(parsedData: ParsedData): Double {
        return parsedData.actions.count { it.type in listOf(ActionType.SCROLL, ActionType.CLICK, ActionType.SEND_KEYS) }.toDouble()
    }
}