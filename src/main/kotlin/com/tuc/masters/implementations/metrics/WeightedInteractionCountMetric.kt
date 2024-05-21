package com.tuc.masters.implementations.metrics

import com.tuc.masters.core.ComplexityMetric
import com.tuc.masters.core.models.*
import org.springframework.stereotype.Component


@Component
class WeightedInteractionCountMetric: ComplexityMetric {
    override var metricsDescription: MetricDescription
        get() = MetricDescription(
            "Weighted interactions count",
            "Calculate interactions sum based on their type",
            listOf(MetricLevel.GROUP, MetricLevel.SINGLE_TEST),
            listOf(ArtifactType.LOG_FILE),
        )
        set(_) {}

    override fun calculateSingleTestMetric(actions: List<InterfaceAction>): Double {
        return actions.sumOf {interactionToWeightMap[it.type] ?: 0}.toDouble()
    }

    private val interactionToWeightMap: Map<ActionType, Int> = mapOf(
        ActionType.CLICK to 1,
        ActionType.SEND_KEYS to 2,
        ActionType.SCROLL to 3,
    )
}