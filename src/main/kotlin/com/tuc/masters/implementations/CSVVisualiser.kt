package com.tuc.masters.implementations

import com.tuc.masters.core.Visualiser
import com.tuc.masters.core.models.EvaluatorConfig
import com.tuc.masters.core.models.GroupData
import com.tuc.masters.core.models.MetricResult
import com.tuc.masters.core.models.TestData
import org.springframework.stereotype.Component
import java.io.File

@Component
class CSVVisualiser : Visualiser {
    override fun visualizeSingleMetrics(config: EvaluatorConfig, data: Map<TestData, List<MetricResult>>) {
        val m = data.values.toMutableList()[0].sortedBy { it.metric.name }
        val results = mutableListOf<String>()
        results.add("test_name,test_file,${m.joinToString { it.metric.name }}")

        for (r in data) {
            results.add(metricToCSVRow("${r.key.testName},${r.key.filePath}", r.value))
        }
        val resultsFile = File("${config.projectPath}/results_single.csv")
        resultsFile.writeText(results.joinToString(separator = "\n"))
    }

    override fun visualizeGroupMetrics(config: EvaluatorConfig, data: Map<GroupData, List<MetricResult>>) {
        val resultsFile = File("${config.projectPath}/results_groups.csv")
        if (data.keys.isEmpty()) {
            resultsFile.writeText("")
            return
        }

        val m = data.values.toMutableList()[0].sortedBy { it.metric.name }
        val results = mutableListOf<String>()
        results.add("group_name,tests,${m.joinToString { it.metric.name }}")

        for (r in data) {
            val joined = r.key.tests.joinToString(separator = "; ") { it.testName }
            results.add(metricToCSVRow("${r.key.groupName},$joined,", r.value))
        }
        resultsFile.writeText(results.joinToString(separator = "\n"))
    }


    private fun metricToCSVRow(name: String, metrics: List<MetricResult>): String {
        val m = metrics.sortedBy { it.metric.name }
        return "${name}, ${m.joinToString { it.value.toString() }}"
    }
}