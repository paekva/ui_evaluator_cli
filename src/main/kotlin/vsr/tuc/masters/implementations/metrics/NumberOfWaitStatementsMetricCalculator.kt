package vsr.tuc.masters.implementations.metrics

import vsr.tuc.masters.core.MetricCalculator
import vsr.tuc.masters.core.models.*
import org.springframework.stereotype.Component


@Component
class NumberOfWaitStatementsMetricCalculator : MetricCalculator {
    override fun getMetricDescription(): MetricDescription {
        return MetricDescription(
            "Average number of wait statements",
            "Calculate (average) number of wait statements in test (tests)",
            listOf(MetricLevel.GROUP, MetricLevel.SINGLE_TEST),
            listOf(ArtifactType.TEST_SOURCE_CODE),
        )
    }

    override fun getSingleTestMetric(testParsedData: ParsedData, logsParsedData: ParsedData?): MetricResult {
        val result = testParsedData.actions.count { it.type == ActionType.WAIT }.toDouble()
        return wrapResult(result)
    }
}