package vsr.tuc.masters.implementations.metrics

import vsr.tuc.masters.core.MetricCalculator
import vsr.tuc.masters.core.models.*

import org.springframework.stereotype.Component


@Component
class NavigationGraphMetricCalculator : MetricCalculator {
    override fun getMetricDescription(): MetricDescription {
        return MetricDescription(
            "Average navigation graph complexity",
            "Calculate (average) complexity of navigation in the test (tests)",
            listOf(MetricLevel.GROUP, MetricLevel.SINGLE_TEST),
            listOf(ArtifactType.LOG_FILE),
        )
    }

    override fun getSingleTestMetric(testParsedData: ParsedData, logsParsedData: ParsedData?): MetricResult {
        val links = (logsParsedData?.actions ?: listOf()).filter { it.type == ActionType.LINK }
        val scrolls = (logsParsedData?.actions ?: listOf()).filter { it.type == ActionType.SCROLL }
        val result = (links.size * 2 + scrolls.size).toDouble()
        return wrapResult(result)
    }

}