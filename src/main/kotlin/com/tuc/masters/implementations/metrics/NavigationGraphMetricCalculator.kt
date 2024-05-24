package com.tuc.masters.implementations.metrics

import com.tuc.masters.core.MetricCalculator
import com.tuc.masters.core.models.*

import org.springframework.stereotype.Component


@Component
class NavigationGraphMetricCalculator: MetricCalculator {
    override var metricsDescription: MetricDescription
        get() = MetricDescription(
            "Navigation graph complexity",
            "Calculate complexity of navigation graph",
            listOf(MetricLevel.GROUP, MetricLevel.SINGLE_TEST),
            listOf(ArtifactType.LOG_FILE),
        )
        set(_) {}

    override fun calculateSingleTestMetric(actions: List<InterfaceAction>): Double {
        val links = actions.filter { it.type == ActionType.SCROLL || it.type== ActionType.LINK }
        val scrolls = actions.filter { it.type == ActionType.SCROLL || it.type== ActionType.LINK }
        return (links.size * 2 + scrolls.size).toDouble()
    }

}