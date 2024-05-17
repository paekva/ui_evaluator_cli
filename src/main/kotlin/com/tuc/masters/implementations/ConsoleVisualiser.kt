package com.tuc.masters.implementations

import com.tuc.masters.core.Visualiser
import com.tuc.masters.core.models.MetricResult
import com.tuc.masters.core.models.TestData
import org.springframework.stereotype.Component

@Component
class ConsoleVisualiser: Visualiser {
    override fun visualize(data: Map<TestData, List<MetricResult>>) {
        data.entries.forEach {
            println("\n\n-----------------------------------------------------")
            println("for test ${it.key.testName} (log are${if(it.key.logs.isEmpty()) " not " else " "}available)")
            println("-----------------------------------------------------")
            it.value.forEach { m ->
                println("${m.metric.name}: ${m.value}")
            }
        }
    }
}