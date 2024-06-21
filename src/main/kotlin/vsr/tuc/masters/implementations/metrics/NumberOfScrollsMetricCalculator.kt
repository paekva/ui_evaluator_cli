package vsr.tuc.masters.implementations.metrics

import vsr.tuc.masters.core.MetricCalculator
import vsr.tuc.masters.core.models.*
import org.springframework.stereotype.Component


@Component
class NumberOfScrollsMetricCalculator : MetricCalculator {
    override fun getMetricDescription(): MetricDescription {
        return MetricDescription(
            "Average number of scrolls",
            "Calculate (average) number of scrolls in test (tests)",
            listOf(MetricLevel.GROUP, MetricLevel.SINGLE_TEST),
            listOf(ArtifactType.LOG_FILE),
        )
    }

    override fun getSingleTestMetric(testParsedData: ParsedData, logsParsedData: ParsedData?): MetricResult {
        val result = (logsParsedData?.actions ?: listOf()).count { it.type == ActionType.SCROLL }.toDouble()
        return wrapResult(result)
    }
}