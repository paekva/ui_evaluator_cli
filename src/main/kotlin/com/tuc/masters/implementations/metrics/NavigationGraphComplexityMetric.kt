package com.tuc.masters.implementations.metrics

import com.tuc.masters.core.ComplexityMetric
import com.tuc.masters.core.models.InterfaceAction
import com.tuc.masters.core.models.MetricDescription
import com.tuc.masters.core.models.MetricLevel

import org.springframework.stereotype.Component


@Component
class NavigationGraphComplexityMetric: ComplexityMetric {
    override var metricsDescription: MetricDescription
        get() = MetricDescription(
            "Navigation graph complexity",
            "Calculate complexity of navigation graph",
            listOf(MetricLevel.GROUP, MetricLevel.SINGLE_TEST),
        )
        set(value) {}

    override fun calculateSingleTestMetric(actions: List<InterfaceAction>): Double {
        // TODO(me): I suppose we can also do like a weighted count,
        //  e.g. each LINK is 2, each scroll is 1, scroll direction change is additional 2, etc
        return 0.0
    }

}