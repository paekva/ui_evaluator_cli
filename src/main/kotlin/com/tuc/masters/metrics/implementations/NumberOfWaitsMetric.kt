package com.tuc.masters.metrics.implementations

import com.tuc.masters.metrics.ComplexityMetric
import com.tuc.masters.metrics.models.*
import org.springframework.stereotype.Component


@Component
class NumberOfWaitsMetric : ComplexityMetric {
    override var metricsDescription: MetricDescription
        get() = MetricDescription(
            "Number of waits in code",
            "Calculate number of wait statements",
            listOf(MetricLevel.GROUP, MetricLevel.SINGLE_TEST),
        )
        set(value) {}

    override fun calculateSingleTestMetric(actions: List<InterfaceAction>): Double {
        return actions.count { it.type == ActionType.WAIT }.toDouble()
    }
}