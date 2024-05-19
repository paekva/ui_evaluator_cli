package com.tuc.masters.core

import com.tuc.masters.core.models.GroupData
import com.tuc.masters.core.models.MetricResult
import com.tuc.masters.core.models.TestData


interface Visualiser {
    fun visualizeSingleMetrics(data: Map<TestData, List<MetricResult>>)
    fun visualizeGroupMetrics(data: Map<GroupData, List<MetricResult>>)
}
