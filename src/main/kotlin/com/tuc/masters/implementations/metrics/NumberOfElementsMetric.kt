package com.tuc.masters.implementations.metrics

import com.tuc.masters.core.ComplexityMetric
import com.tuc.masters.core.models.*
import org.springframework.stereotype.Component


@Component
class NumberOfElementsMetric : ComplexityMetric {
    override var metricsDescription: MetricDescription
        get() = MetricDescription(
            "Number of elements",
            "Calculate number of elements mentioned in the test",
            listOf(MetricLevel.GROUP, MetricLevel.SINGLE_TEST),
            listOf(ArtifactType.LOG_FILE),
        )
        set(value) {}

    override fun calculateSingleTestMetric(actions: List<InterfaceAction>): Double {
        val rgx = Regex("\"value\": \"(?<name>\\S+)\"")
        val clicks = actions.filter {
            listOf(
                ActionType.CLICK,
                ActionType.SEND_KEYS,
            ).contains(it.type)
        }
        val tmp = actions.filter {
            listOf(
                ActionType.CLICK,
                ActionType.FIND,
                ActionType.SEND_KEYS,
                ActionType.FIND_ALL
            ).contains(it.type)
        }

        val elements = mutableListOf<String>()
        tmp.forEach {
            val k = rgx.find(it.args.toString())
            val g = k?.groups?.get(1)?.value ?: ""
            println("${it.type} - $g")
            elements.add(g)
        }
        val uniqueEl = elements.toSet()
        // TODO(me): need to introduce Element field for some type of actions, e.g. Click, SendKeys, isDisplayed
        //  this way we can extract them all and check their ids, xpaths, etc. and calculate the uniques number
        return 0.0
    }
}