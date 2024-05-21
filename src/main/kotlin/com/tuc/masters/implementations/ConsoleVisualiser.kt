package com.tuc.masters.implementations

import com.tuc.masters.core.Visualiser
import com.tuc.masters.core.models.EvaluatorConfig
import com.tuc.masters.core.models.GroupData
import com.tuc.masters.core.models.MetricResult
import com.tuc.masters.core.models.TestData
import de.m3y.kformat.Table
import de.m3y.kformat.table
import org.springframework.stereotype.Component

@Component
class ConsoleVisualiser : Visualiser {
    override fun visualizeSingleMetrics(config: EvaluatorConfig, data: Map<TestData, List<MetricResult>>) {
        val header = arrayListOf("test name")
        header.addAll(data.entries.toList()[0].value.map { it.metric.name })

        drawTable(header, data.entries.associate { (key, value) ->
            key.testName to value
        })
    }

    override fun visualizeGroupMetrics(config: EvaluatorConfig, data: Map<GroupData, List<MetricResult>>) {
        val header = arrayListOf("group name")
        header.addAll(data.entries.toList()[0].value.map { it.metric.name })

        drawTable(header, data.entries.associate { (key, value) ->
            key.groupName to value
        })
    }

    private fun drawTable(headers: List<String>, data: Map<String, List<MetricResult>>) {
        table {
            header(headers)
            data.forEach { e ->
                val values = arrayListOf(e.key)
                values.addAll(e.value.map { it.value.toString() })
                header(values)
            }
            row()

            hints {

                borderStyle = Table.BorderStyle.SINGLE_LINE // or NONE
            }
        }.print()
    }
}