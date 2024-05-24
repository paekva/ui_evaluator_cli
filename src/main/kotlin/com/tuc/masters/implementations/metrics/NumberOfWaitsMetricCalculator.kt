package com.tuc.masters.implementations.metrics

import com.tuc.masters.core.MetricCalculator
import com.tuc.masters.core.models.*
import org.springframework.stereotype.Component


@Component
class NumberOfWaitsMetricCalculator : MetricCalculator {
    override var metricsDescription: MetricDescription
        get() = MetricDescription(
            "Number of waits in code",
            "Calculate number of wait statements",
            listOf(MetricLevel.GROUP, MetricLevel.SINGLE_TEST),
            listOf(ArtifactType.TEST_SOURCE_CODE),
        )
        set(_) {}

    override fun calculateSingleTestMetric(actions: List<InterfaceAction>): Double {
        return actions.count { it.type == ActionType.WAIT }.toDouble()
    }
}