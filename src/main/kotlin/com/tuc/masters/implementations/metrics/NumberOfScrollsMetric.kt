package com.tuc.masters.implementations.metrics

import com.tuc.masters.core.ComplexityMetric
import com.tuc.masters.core.models.*
import org.springframework.stereotype.Component


@Component
class NumberOfScrollsMetric : ComplexityMetric {
    override var metricsDescription: MetricDescription
        get() = MetricDescription(
            "Number of scrolls",
            "Calculate scrolls",
            listOf(MetricLevel.GROUP, MetricLevel.SINGLE_TEST),
            listOf(ArtifactType.LOG_FILE),
        )
        set(_) {}

    override fun calculateSingleTestMetric(actions: List<InterfaceAction>): Double {
        return actions.count { it.type == ActionType.SCROLL }.toDouble()
    }
}