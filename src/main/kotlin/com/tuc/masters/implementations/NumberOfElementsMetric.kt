package com.tuc.masters.implementations

import com.tuc.masters.metrics.ComplexityMetric
import com.tuc.masters.metrics.models.*
import org.springframework.stereotype.Component


@Component
class NumberOfElementsMetric : ComplexityMetric {
    override var metricsDescription: MetricDescription
        get() = MetricDescription(
            "Number of elements ",
            "Calculate number of elements mentioned in the test",
            listOf(MetricLevel.GROUP, MetricLevel.SINGLE_TEST),
        )
        set(value) {}

    override fun calculateSingleTestMetric(actions: List<InterfaceAction>): Double {
        // TODO(me): need to introduce Element field for some type of actions, e.g. Click, SendKeys, isDisplayed
        //  this way we can extract them all and check their ids, xpaths, etc. and calculate the uniques number
        return 0.0
    }
}