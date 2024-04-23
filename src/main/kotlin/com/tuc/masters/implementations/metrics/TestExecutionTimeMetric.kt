package com.tuc.masters.implementations.metrics

import com.tuc.masters.core.ComplexityMetric
import com.tuc.masters.core.models.*
import org.springframework.stereotype.Component


@Component
class TestExecutionTimeMetric : ComplexityMetric {
    override var metricsDescription: MetricDescription
        get() = MetricDescription(
            "Test execution time",
            "Time it takes to perform the test based on the log data",
            listOf(MetricLevel.GROUP, MetricLevel.SINGLE_TEST),
            listOf(ArtifactType.LOG_FILE),
        )
        set(value) {}

    override fun calculateSingleTestMetric(actions: List<InterfaceAction>): Double {
        val start = actions.firstOrNull { it.type == ActionType.START }?.timestamp?.toDouble() ?: 0.0
        val stop = actions.firstOrNull { it.type == ActionType.STOP }?.timestamp?.toDouble() ?: 0.0

        return stop - start
    }
}