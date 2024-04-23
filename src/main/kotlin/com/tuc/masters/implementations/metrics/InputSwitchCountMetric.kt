package com.tuc.masters.implementations.metrics

import com.tuc.masters.core.ComplexityMetric
import com.tuc.masters.core.models.*
import org.springframework.stereotype.Component


@Component
class InputSwitchCountMetric: ComplexityMetric {
    override var metricsDescription: MetricDescription
        get() = MetricDescription(
            "Input switch count",
            "How often the input type (mouse vs keyboard) is changed",
            listOf(MetricLevel.GROUP, MetricLevel.SINGLE_TEST),
            listOf(ArtifactType.LOG_FILE),
        )
        set(value) {}

    override fun calculateSingleTestMetric(actions: List<InterfaceAction>): Double {
        var count = 0
        val inputActions = actions.filter { it.type in inputActions }
        var isKeyboard = inputActions.first().type == ActionType.SEND_KEYS
        for (a in inputActions) {
            if(isKeyboard && a.type != ActionType.SEND_KEYS || !isKeyboard && a.type == ActionType.SEND_KEYS) {
                count++
                isKeyboard = !isKeyboard
            }
        }

        return count.toDouble()
    }

    private var inputActions = listOf(ActionType.SEND_KEYS, ActionType.SCROLL, ActionType.CLICK)
}