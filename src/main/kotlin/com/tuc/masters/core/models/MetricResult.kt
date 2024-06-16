package com.tuc.masters.core.models

// Describes data that is the result of metric calculation
data class MetricResult (
    var metric: MetricDescription,
    var value: Double,
    var level: MetricLevel,
)