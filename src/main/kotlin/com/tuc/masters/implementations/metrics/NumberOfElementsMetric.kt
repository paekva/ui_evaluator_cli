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
        val rgx = Regex("\"id\": \"(?<name>\\S+)\"")
        val rgx2 = Regex("\"element[0-9a-z-]+\": \"(?<name>\\S+)\"")
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
            val regex = if (it.type == ActionType.FIND || it.type == ActionType.FIND_ALL) rgx2 else rgx
            val k = regex.find(it.args.toString())
            val res = k?.groups?.get(1)?.value ?: ""
            elements.add(res)
        }
        val uniqueEl = elements.toSet().toList().filter { it.isNotBlank() }
        return uniqueEl.size.toDouble()
        // TODO(me): need to introduce Element field for some type of actions, e.g. Click, SendKeys, isDisplayed
        //  this way we can extract them all and check their ids, xpaths, etc. and calculate the uniques number
    }
}