package com.tuc.masters.implementations.metrics

import com.tuc.masters.core.MetricCalculator
import com.tuc.masters.core.models.*
import org.springframework.stereotype.Component


@Component
class NumberOfInputTypeSwitchMetricCalculator : MetricCalculator {
    override var metricsDescription: MetricDescription
        get() = MetricDescription(
            "Average number of input type switch per test",
            "How often the input type (mouse vs keyboard) is changed",
            listOf(MetricLevel.GROUP, MetricLevel.SINGLE_TEST),
            listOf(ArtifactType.LOG_FILE),
        )
        set(_) {}

    override fun calculateSingleTestMetric(parsedData: ParsedData): Double {
        var count = 0
        val inputActions = parsedData.actions.filter { it.type in inputActions }
        var isKeyboard = inputActions.firstOrNull()?.type == ActionType.SEND_KEYS
        for (a in inputActions) {
            if (isKeyboard && a.type != ActionType.SEND_KEYS || !isKeyboard && a.type == ActionType.SEND_KEYS) {
                count++
                isKeyboard = !isKeyboard
            }
        }

        return count.toDouble()
    }

    private var inputActions = listOf(ActionType.SEND_KEYS, ActionType.SCROLL, ActionType.CLICK)
}