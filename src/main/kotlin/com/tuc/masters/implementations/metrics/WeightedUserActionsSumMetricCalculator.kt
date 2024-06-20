package com.tuc.masters.implementations.metrics

import com.tuc.masters.core.MetricCalculator
import com.tuc.masters.core.models.*
import org.springframework.stereotype.Component


@Component
class WeightedUserActionsSumMetricCalculator : MetricCalculator {
    override fun getMetricDescription(): MetricDescription {
        return MetricDescription(
            "Average weighted user actions sum",
            "Calculate (average) interactions sum based on their type",
            listOf(MetricLevel.GROUP, MetricLevel.SINGLE_TEST),
            listOf(ArtifactType.LOG_FILE),
        )
    }


    // 1.10 s for pointing
    // 0.20s for btn click or keystroke
    // 0.40s homing (changing input device)
    override fun getSingleTestMetric(testParsedData: ParsedData, logsParsedData: ParsedData?): MetricResult {
        var sum = 0.0
        var isKeyboard = (logsParsedData?.actions ?: listOf()).firstOrNull()?.type == ActionType.SEND_KEYS
        for (a in (logsParsedData?.actions ?: listOf())) {
            if (a.type == ActionType.SEND_KEYS) {
                val number = getNumberOfKeys(a.args ?: "") ?: 1
                sum += 0.2 * number
            }
            if (a.type == ActionType.CLICK) sum += 1.3

            if (isKeyboard && a.type != ActionType.SEND_KEYS || !isKeyboard && a.type == ActionType.SEND_KEYS) {
                isKeyboard = !isKeyboard
                sum += 0.4
            }
        }
        return wrapResult(sum)
    }

    private fun getNumberOfKeys(args: String): Int? {
        val rgx = Regex("\"text\"\\s*:\\s*\"(?<name>[\\S\\s]*)\"")
        val res = rgx.find(args) ?: return null

        return res.groups[1]?.value?.length
    }
}