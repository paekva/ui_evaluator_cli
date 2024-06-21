package vsr.tuc.masters.implementations.metrics

import vsr.tuc.masters.core.MetricCalculator
import vsr.tuc.masters.core.models.*
import org.springframework.stereotype.Component


@Component
class NumberOfUserActionsMetricCalculator : MetricCalculator {
    override fun getMetricDescription(): MetricDescription {
        return MetricDescription(
            "Average number of user actions",
            "Calculate (average) number of user actions in test (tests) \n" +
                    "Actions: clicks, keyboard interaction, scrolls, etc.",
            listOf(MetricLevel.GROUP, MetricLevel.SINGLE_TEST),
            listOf(ArtifactType.LOG_FILE),
        )
    }

    override fun getSingleTestMetric(testParsedData: ParsedData, logsParsedData: ParsedData?): MetricResult {
        val result =
            (logsParsedData?.actions ?: listOf()).count {
                it.type in listOf(
                    ActionType.SCROLL,
                    ActionType.CLICK,
                    ActionType.SEND_KEYS
                )
            }
                .toDouble()
        return wrapResult(result)
    }
}