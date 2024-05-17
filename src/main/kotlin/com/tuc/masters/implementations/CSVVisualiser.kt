package com.tuc.masters.implementations

import com.tuc.masters.core.Visualiser
import com.tuc.masters.core.models.MetricResult
import com.tuc.masters.core.models.TestData
import org.springframework.stereotype.Component
import java.io.File

@Component
class CSVVisualiser : Visualiser {
    private val projectPath = "/Users/paekva/projects/masters_materials/zm-selenium" // TODO: move to config or such
    override fun visualize(data: Map<TestData, List<MetricResult>>) {
        val m = data.values.toMutableList()[0].sortedBy { it.metric.name }
        val results = mutableListOf<String>()
        results.add("test_name,${m.joinToString { it.metric.name }}")

        for (r in data) {
            results.add(metricToCSVRow(r.key, r.value))
        }
        val resultsFile = File("$projectPath/results.csv")
        resultsFile.writeText(results.joinToString(separator = "\n"))
    }


    private fun metricToCSVRow(test: TestData, metrics: List<MetricResult>): String {
        val m = metrics.sortedBy { it.metric.name }
        return "${test.testName}, ${m.joinToString { it.value.toString() }}"
    }
}