package vsr.tuc.masters.implementations.metrics

import vsr.tuc.masters.core.MetricCalculator
import vsr.tuc.masters.core.models.*
import org.springframework.stereotype.Component


@Component
class NumberOfHomingActionsMetricCalculator : MetricCalculator {
    override fun getMetricDescription(): MetricDescription {
        return MetricDescription(
            "Average number of homing actions",
            "Calculate (average) number of homing actions in test (tests)\n" +
                    "Homing: how often the user is required to change between keyboard and mouse",
            listOf(MetricLevel.GROUP, MetricLevel.SINGLE_TEST),
            listOf(ArtifactType.LOG_FILE),
        )
    }

    override fun getSingleTestMetric(testParsedData: ParsedData, logsParsedData: ParsedData?): MetricResult {
        var count = 0
        val inputActions = (logsParsedData?.actions ?: listOf()).filter { it.type in inputActions }
        var isKeyboard = inputActions.firstOrNull()?.type == ActionType.SEND_KEYS
        for (a in inputActions) {
            if (isKeyboard && a.type != ActionType.SEND_KEYS || !isKeyboard && a.type == ActionType.SEND_KEYS) {
                count++
                isKeyboard = !isKeyboard
            }
        }

        val result = count.toDouble()
        return wrapResult(result)
    }

    private var inputActions = listOf(ActionType.SEND_KEYS, ActionType.SCROLL, ActionType.CLICK)
}