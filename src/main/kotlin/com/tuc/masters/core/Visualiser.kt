package com.tuc.masters.core

import com.tuc.masters.core.models.EvaluatorConfig
import com.tuc.masters.core.models.GroupData
import com.tuc.masters.core.models.MetricResult
import com.tuc.masters.core.models.TestData

// Describes the functionality that a visualiser of calculation results should have
interface Visualiser {
    fun visualizeSingleMetrics(config: EvaluatorConfig, data: Map<TestData, List<MetricResult>>)
    fun visualizeGroupMetrics(config: EvaluatorConfig, data: Map<GroupData, List<MetricResult>>)
}
