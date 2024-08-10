package vsr.tuc.masters.implementations.visualisers

import vsr.tuc.masters.core.Visualiser
import vsr.tuc.masters.core.models.EvaluatorConfig
import vsr.tuc.masters.core.models.GroupData
import vsr.tuc.masters.core.models.MetricResult
import vsr.tuc.masters.core.models.TestData
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

        val size = data.entries.toList()[0].value.size
        for (i in 0..size step 2) {
            val end = if (i + 2 < size) i + 2 else i + 1
            val header = arrayListOf("group name")

            header.addAll(data.entries.toList()[0].value.subList(i, end)
                .map { replaceEverySecondSpaceWithNewline(it.metric.name) })

            drawTable(header, data.entries.associate { (key, value) ->
                key.groupName to value.subList(i, end)
            })
        }
    }

    private fun replaceEverySecondSpaceWithNewline(input: String): String {
        val parts = input.split(' ')
        var result = ""

        for (i in parts.indices step 2) {
            val s = " " + if(i+1 < parts.size) parts[i + 1] else ""
            result += parts[i] + s + "\n"
        }

        return result
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