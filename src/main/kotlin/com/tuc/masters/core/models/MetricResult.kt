package com.tuc.masters.core.models

data class MetricResult (
    var metric: MetricDescription,
    var value: Double,
    var level: MetricLevel,
)