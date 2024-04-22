package com.tuc.masters.metrics.models

//TODO: add toString implementation as requirement
data class MetricDescription (
    var name: String,
    var description: String,
    var availableLevels: List<MetricLevel>
)
