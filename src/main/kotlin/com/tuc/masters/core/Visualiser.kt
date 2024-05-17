package com.tuc.masters.core

import com.tuc.masters.core.models.MetricResult
import com.tuc.masters.core.models.TestData


interface Visualiser {
    fun visualize(data: Map<TestData, List<MetricResult>>)
}
