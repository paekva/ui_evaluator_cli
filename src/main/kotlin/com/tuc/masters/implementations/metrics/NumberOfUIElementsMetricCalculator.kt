package com.tuc.masters.implementations.metrics

import com.tuc.masters.core.MetricCalculator
import com.tuc.masters.core.models.*
import org.springframework.stereotype.Component


@Component
class NumberOfUIElementsMetricCalculator : MetricCalculator {
    override var metricsDescription: MetricDescription
        get() = MetricDescription(
            "Average number of UI elements",
            "Calculate (average) number of elements in test (tests)",
            listOf(MetricLevel.GROUP, MetricLevel.SINGLE_TEST),
            listOf(ArtifactType.LOG_FILE),
        )
        set(_) {}

    override fun getSingleTestMetric(testParsedData: ParsedData, logsParsedData: ParsedData?): MetricResult {
        val rgx = Regex("\"id\": \"(?<name>\\S+)\"")
        val rgx2 = Regex("\"element[0-9a-z-]+\": \"(?<name>\\S+)\"")
        val tmp = (logsParsedData?.actions ?: listOf()).filter {
            listOf(
                ActionType.CLICK,
                ActionType.SEND_KEYS,
                ActionType.SCROLL,
                ActionType.FIND,
                ActionType.FIND_ALL,
                ActionType.IS_DISPLAYED,
                ActionType.GET_PROPERTY,
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
        val result = uniqueEl.size.toDouble()

        return wrapResult(result)
    }
}