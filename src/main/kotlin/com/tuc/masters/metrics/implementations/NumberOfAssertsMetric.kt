package com.tuc.masters.metrics.implementations

import com.tuc.masters.metrics.ComplexityMetric
import com.tuc.masters.metrics.models.*
import org.springframework.stereotype.Component


@Component
class NumberOfAssertsMetric : ComplexityMetric {
    override var metricsDescription: MetricDescription
        get() = MetricDescription(
            "Number of asserts in code",
            "Calculate number of assert statements",
            listOf(MetricLevel.GROUP, MetricLevel.SINGLE_TEST),
        )
        set(value) {}

    override fun calculateSingleTestMetric(actions: List<InterfaceAction>): Double {
        return actions.count { it.type == ActionType.ASSERT }.toDouble()
    }
}