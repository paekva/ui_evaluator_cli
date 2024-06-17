package com.tuc.masters.implementations.visualisers

import com.tuc.masters.core.Visualiser
import com.tuc.masters.core.models.EvaluatorConfig
import com.tuc.masters.core.models.GroupData
import com.tuc.masters.core.models.MetricResult
import com.tuc.masters.core.models.TestData
import mu.KLogging
import org.springframework.stereotype.Component
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode

// Implementation of the [Visualiser] for saving to CSV files
@Component
class CSVVisualiser : Visualiser {
    companion object : KLogging()

    override fun visualizeSingleMetrics(config: EvaluatorConfig, data: Map<TestData, List<MetricResult>>) {
        if (data.entries.toList().isEmpty()) {
            logger.warn { "No data found to visualise for single metrics" }
            return
        }

        val m = data.values.toMutableList()[0].sortedBy { it.metric.name }
        val results = mutableListOf<String>()
        results.add("test_name,test_file,has_errors,${m.joinToString { it.metric.name }}")

        for (r in data) {
            results.add(
                metricToCSVRow(
                    "${r.key.testName},${r.key.filePath},${r.key.logs?.actions?.any { it.hasError }}",
                    r.value
                )
            )
        }
        val resultsFile = File("${config.projectPath}/results_single.csv")
        resultsFile.writeText(results.joinToString(separator = "\n"))
    }

    override fun visualizeGroupMetrics(config: EvaluatorConfig, data: Map<GroupData, List<MetricResult>>) {
        if (data.entries.toList().isEmpty() || data.values.flatten().isEmpty()) {
            logger.warn { "No data found to visualise for group metrics" }
            return
        }

        val resultsFile = File("${config.projectPath}/results_groups.csv")
        if (data.keys.isEmpty()) {
            resultsFile.writeText("")
            return
        }

        val m = data.values.toMutableList()[0].sortedBy { it.metric.name }
        val results = mutableListOf<String>()
        results.add("group_name,tests,${m.joinToString { it.metric.name }}")

        for (r in data) {
            results.add(metricToCSVRow("${r.key.groupName},${r.key.tests.size}", r.value))
        }
        resultsFile.writeText(results.joinToString(separator = "\n"))
    }


    private fun metricToCSVRow(name: String, metrics: List<MetricResult>): String {
        val m = metrics.sortedBy { it.metric.name }
        return "${name}, ${m.joinToString { BigDecimal(it.value).setScale(4, RoundingMode.HALF_EVEN).toString() }}"
    }
}