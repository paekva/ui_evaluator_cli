package com.tuc.masters.implementations.metrics

import com.tuc.masters.core.MetricCalculator
import com.tuc.masters.core.models.*
import org.springframework.stereotype.Component


@Component
class NumberOfInteractionOptionsMetricCalculator: MetricCalculator {
    override var metricsDescription: MetricDescription
        get() = MetricDescription(
            "Number of interaction options",
            "Average number of interaction options per test",
            listOf(MetricLevel.GROUP, MetricLevel.SINGLE_TEST),
            listOf(ArtifactType.LOG_FILE),
        )
        set(_) {}

    override fun calculateSingleTestMetric(parsedData: ParsedData): Double {
        // TODO(me): 1. need to find a way to identify complex elements, e.g. radio btns, selectors, button groups (xpath)
        //  2. calculate approx number of options per element
        //  3. do the average for a test (do not forget, that 1 option for interaction is counted as 1)
        return 0.0
    }
}