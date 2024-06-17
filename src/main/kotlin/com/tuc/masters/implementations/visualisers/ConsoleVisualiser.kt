package com.tuc.masters.implementations.visualisers

import com.tuc.masters.core.Visualiser
import com.tuc.masters.core.models.EvaluatorConfig
import com.tuc.masters.core.models.GroupData
import com.tuc.masters.core.models.MetricResult
import com.tuc.masters.core.models.TestData
import de.m3y.kformat.Table
import de.m3y.kformat.table
import mu.KLogging
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode

// Implementation of the [Visualiser] for printing to console
@Component
class ConsoleVisualiser : Visualiser {
    companion object : KLogging()
    override fun visualizeSingleMetrics(config: EvaluatorConfig, data: Map<TestData, List<MetricResult>>) {
        if (data.entries.toList().isEmpty()) {
            logger.warn { "No data found to visualise for single metrics" }
            return
        }

        val header = arrayListOf("test name")
        header.addAll(data.entries.toList()[0].value.map { it.metric.name })

        drawTable(header, data.entries.associate { (key, value) ->
            key.testName to value
        })
    }

    override fun visualizeGroupMetrics(config: EvaluatorConfig, data: Map<GroupData, List<MetricResult>>) {
        if (data.entries.toList().isEmpty() || data.values.flatten().isEmpty()) {
            logger.warn { "No data found to visualise for group metrics" }
            return
        }

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
                values.addAll(e.value.map { BigDecimal(it.value).setScale(4, RoundingMode.HALF_EVEN).toString() })
                header(values)
            }
            row()

            hints {
                borderStyle = Table.BorderStyle.SINGLE_LINE
            }
        }.print()
    }
}