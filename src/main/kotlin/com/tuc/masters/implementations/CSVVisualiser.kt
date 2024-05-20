package com.tuc.masters.implementations

import com.tuc.masters.core.Visualiser
import com.tuc.masters.core.models.GroupData
import com.tuc.masters.core.models.MetricResult
import com.tuc.masters.core.models.TestData
import org.springframework.stereotype.Component
import java.io.File

@Component
class CSVVisualiser : Visualiser {
    private val projectPath = "/Users/paekva/projects/masters_materials/zm-selenium" // TODO: move to config or such
    override fun visualizeSingleMetrics(data: Map<TestData, List<MetricResult>>) {
        val m = data.values.toMutableList()[0].sortedBy { it.metric.name }
        val results = mutableListOf<String>()
        results.add("test_name,${m.joinToString { it.metric.name }}")

        for (r in data) {
            results.add(metricToCSVRow(r.key.testName, r.value))
        }
        val resultsFile = File("$projectPath/results_single.csv")
        resultsFile.writeText(results.joinToString(separator = "\n"))
    }

    override fun visualizeGroupMetrics(data: Map<GroupData, List<MetricResult>>) {
        val resultsFile = File("$projectPath/results_groups.csv")
        if (data.keys.isEmpty()) {
            resultsFile.writeText("")
            return
        }

        val m = data.values.toMutableList()[0].sortedBy { it.metric.name }
        val results = mutableListOf<String>()
        results.add("group_name,${m.joinToString { it.metric.name }}")

        for (r in data) {
            results.add(metricToCSVRow(r.key.groupName, r.value))
        }
        resultsFile.writeText(results.joinToString(separator = "\n"))
    }


    private fun metricToCSVRow(name: String, metrics: List<MetricResult>): String {
        val m = metrics.sortedBy { it.metric.name }
        return "${name}, ${m.joinToString { it.value.toString() }}"
    }
}