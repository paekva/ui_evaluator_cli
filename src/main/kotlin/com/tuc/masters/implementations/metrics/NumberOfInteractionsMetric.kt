package com.tuc.masters.implementations.metrics

import com.tuc.masters.core.ComplexityMetric
import com.tuc.masters.core.models.ActionType
import com.tuc.masters.core.models.InterfaceAction
import com.tuc.masters.core.models.MetricDescription
import com.tuc.masters.core.models.MetricLevel
import org.springframework.stereotype.Component


@Component
class NumberOfInteractionsMetric: ComplexityMetric {
    override var metricsDescription: MetricDescription
        get() = MetricDescription(
            "Number of actions",
            "Calculate actions, such as clicks, keyboard interaction, scrolls, etc.",
            listOf(MetricLevel.GROUP, MetricLevel.SINGLE_TEST),
        )
        set(value) {}

    override fun calculateSingleTestMetric(actions: List<InterfaceAction>): Double {
        return actions.count { it.type in listOf(ActionType.SCROLL, ActionType.CLICK, ActionType.SEND_KEYS) }.toDouble()
    }
}