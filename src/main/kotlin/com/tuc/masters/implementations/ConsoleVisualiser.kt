package com.tuc.masters.implementations

import com.tuc.masters.core.Visualiser
import com.tuc.masters.core.models.EvaluatorConfig
import com.tuc.masters.core.models.GroupData
import com.tuc.masters.core.models.MetricResult
import com.tuc.masters.core.models.TestData
import org.springframework.stereotype.Component

@Component
class ConsoleVisualiser : Visualiser {
    override fun visualizeSingleMetrics(config: EvaluatorConfig, data: Map<TestData, List<MetricResult>>) {
        data.entries.forEach {
            println("\n\n-----------------------------------------------------")
            println("for test ${it.key.testName} (log are${if (it.key.logs.isEmpty()) " not " else " "}available)")
            println("-----------------------------------------------------")
            it.value.forEach { m ->
                println("${m.metric.name}: ${m.value}")
            }
        }
    }

    override fun visualizeGroupMetrics(config: EvaluatorConfig, data: Map<GroupData, List<MetricResult>>) {
        data.entries.forEach {
            println("\n\n-----------------------------------------------------")
            println("for group ${it.key.groupName} (${it.key.tests.joinToString { t -> t.testName }})")
            println("-----------------------------------------------------")
            it.value.forEach { m ->
                println("${m.metric.name}: ${m.value}")
            }
        }
    }
}