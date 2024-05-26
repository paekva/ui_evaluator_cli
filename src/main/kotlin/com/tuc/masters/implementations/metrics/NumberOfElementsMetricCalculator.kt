package com.tuc.masters.implementations.metrics

import com.tuc.masters.core.MetricCalculator
import com.tuc.masters.core.models.*
import org.springframework.stereotype.Component


@Component
class NumberOfElementsMetricCalculator : MetricCalculator {
    override var metricsDescription: MetricDescription
        get() = MetricDescription(
            "Number of elements",
            "Calculate number of elements mentioned in the test",
            listOf(MetricLevel.GROUP, MetricLevel.SINGLE_TEST),
            listOf(ArtifactType.LOG_FILE),
        )
        set(_) {}

    override fun calculateSingleTestMetric(parsedData: ParsedData): Double {
        val rgx = Regex("\"id\": \"(?<name>\\S+)\"")
        val rgx2 = Regex("\"element[0-9a-z-]+\": \"(?<name>\\S+)\"")
        val tmp = parsedData.actions.filter {
            listOf(
                ActionType.CLICK,
                ActionType.SEND_KEYS,
                ActionType.SCROLL,
                ActionType.FIND,
                ActionType.FIND_ALL,
                ActionType.IS_DISPLAYED
            ).contains(it.type)
        }

        val elements = mutableListOf<String>()
        tmp.forEach {
            val regex = if (it.type == ActionType.CLICK || it.type == ActionType.SEND_KEYS) rgx else rgx2
            val k = regex.find(it.args.toString())
            val res = k?.groups?.get(1)?.value ?: ""
            elements.add(res)
        }
        val uniqueEl = elements.toSet().toList().filter { it.isNotBlank() }
        return uniqueEl.size.toDouble()
    }
}