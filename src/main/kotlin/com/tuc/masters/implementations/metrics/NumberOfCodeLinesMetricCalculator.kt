package com.tuc.masters.implementations.metrics

import com.tuc.masters.core.MetricCalculator
import com.tuc.masters.core.models.ArtifactType
import com.tuc.masters.core.models.InterfaceAction
import com.tuc.masters.core.models.MetricDescription
import com.tuc.masters.core.models.MetricLevel
import org.springframework.stereotype.Component

@Component
class NumberOfCodeLinesMetricCalculator : MetricCalculator {
    override var metricsDescription: MetricDescription
        get() = MetricDescription(
            "Number of line in test's code",
            "Calculate number of code lines",
            listOf(MetricLevel.GROUP, MetricLevel.SINGLE_TEST),
            listOf(ArtifactType.TEST_SOURCE_CODE),
        )
        set(_) {}

    override fun calculateSingleTestMetric(actions: List<InterfaceAction>): Double {
        return actions.count().toDouble()
    }
}