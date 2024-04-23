package com.tuc.masters.implementations.metrics

import com.tuc.masters.core.ComplexityMetric
import com.tuc.masters.core.models.*
import org.springframework.stereotype.Component


@Component
class NumberOfAssertsMetric : ComplexityMetric {
    override var metricsDescription: MetricDescription
        get() = MetricDescription(
            "Number of asserts in code",
            "Calculate number of assert statements",
            listOf(MetricLevel.GROUP, MetricLevel.SINGLE_TEST),
            listOf(ArtifactType.TEST_SOURCE_CODE),
        )
        set(value) {}

    override fun calculateSingleTestMetric(actions: List<InterfaceAction>): Double {
        return actions.count { it.type == ActionType.ASSERT }.toDouble()
    }
}