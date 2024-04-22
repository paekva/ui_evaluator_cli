package com.tuc.masters.metrics.implementations

import com.tuc.masters.metrics.ComplexityMetric
import com.tuc.masters.metrics.models.*
import org.springframework.stereotype.Component


@Component
class TestExecutionTimeMetric : ComplexityMetric {
    override var metricsDescription: MetricDescription
        get() = MetricDescription(
            "Test execution time",
            "Time it takes to perform the test based on the log data",
            listOf(MetricLevel.GROUP, MetricLevel.SINGLE_TEST),
        )
        set(value) {}

    override fun calculateSingleTestMetric(actions: List<InterfaceAction>): Double {
        val start = actions.firstOrNull { it.type == ActionType.START }?.args?.get(0)?.toDouble() ?: 0.0
        val stop = actions.firstOrNull { it.type == ActionType.STOP }?.args?.get(0)?.toDouble() ?: 0.0

        return stop - start
    }
}