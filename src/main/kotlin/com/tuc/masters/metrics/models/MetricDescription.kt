package com.tuc.masters.metrics.models

data class MetricDescription (
    var name: String,
    var description: String,
    var availableLevels: List<MetricLevel>
)
