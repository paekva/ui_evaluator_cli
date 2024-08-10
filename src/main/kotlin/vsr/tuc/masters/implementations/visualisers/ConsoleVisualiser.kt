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

    val columnWidth = 21

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

        drawTableNew(data)
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


    private fun drawTableNew(data: Map<GroupData, List<MetricResult>>) {
        val header = data.entries.toList()[0].value.map { it.metric.name }.toMutableList()
        header.add(0, "group name")
        printTableHeader(header)

        for (i in header) {
            print("---------------------")
        }
        println()

        val formatted = data.entries.map {
            val res = mutableListOf(it.key.groupName)
            res.addAll(it.value.map { m -> BigDecimal(m.value).setScale(4, RoundingMode.HALF_EVEN).toString() }
                .toList())

            res.toList()
        }.toMutableList()
        printTableWithFixedWidth(formatted)
    }

    fun printTableHeader(headers: List<String>) {
        // Find the maximum number of lines needed for any header
        val wrappedHeaders = headers.map { wrapHeader(it, columnWidth) }
        val maxLines = wrappedHeaders.map { it.size }.maxOrNull() ?: 1

        // Print each line of the header
        for (line in 0 until maxLines) {
            wrappedHeaders.forEach { wrappedHeader ->
                val part = if (line < wrappedHeader.size) wrappedHeader[line] else ""
                print(part.padEnd(columnWidth))
            }
            println()
        }
    }

    fun wrapHeader(header: String, columnWidth: Int): List<String> {
        val words = header.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            if (currentLine.length + word.length + 1 > columnWidth) {
                lines.add(currentLine.toString())
                currentLine = StringBuilder()
            }
            if (currentLine.isNotEmpty()) {
                currentLine.append(" ")
            }
            currentLine.append(word)
        }

        // Add the last line if not empty
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }

        return lines
    }

    fun printTableWithFixedWidth(table: List<List<String>>) {
        table.forEach { row ->
            row.forEach { cell ->
                val formattedCell = if (cell.length > columnWidth) {
                    val partLength = (columnWidth - 3) / 2
                    val start = cell.substring(0, partLength)
                    val end = cell.substring(cell.length - partLength)
                    "$start...$end"
                } else {
                    cell.padEnd(columnWidth)
                }
                print(formattedCell)
            }
            println() // Move to the next row
        }
    }
}