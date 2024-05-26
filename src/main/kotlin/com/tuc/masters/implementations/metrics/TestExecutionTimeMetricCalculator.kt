package com.tuc.masters.implementations.metrics

import com.tuc.masters.core.MetricCalculator
import com.tuc.masters.core.models.*
import org.springframework.stereotype.Component


@Component
class TestExecutionTimeMetricCalculator : MetricCalculator {
    override var metricsDescription: MetricDescription
        get() = MetricDescription(
            "Test execution time",
            "Time it takes to perform the test based on the log data",
            listOf(MetricLevel.GROUP, MetricLevel.SINGLE_TEST),
            listOf(ArtifactType.LOG_FILE),
        )
        set(_) {}

    override fun calculateSingleTestMetric(actions: List<InterfaceAction>): Double {
        val start = actions.firstOrNull { it.type == ActionType.START }?.timestamp?.toDouble() ?: 0.0
        val stop = actions.firstOrNull { it.type == ActionType.STOP }?.timestamp?.toDouble() ?: 0.0

        return if(stop == 0.0 || start == 0.0) 0.0 else stop - start
    }

    override fun calculateGroupTestMetric(results: List<MetricResult>): Double {
        return super.calculateGroupTestMetric(results.filter { it.value != 0.0 })
    }
}