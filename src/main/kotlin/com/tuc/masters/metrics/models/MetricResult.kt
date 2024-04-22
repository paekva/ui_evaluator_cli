package com.tuc.masters.metrics.models

data class MetricResult (
    var metric: MetricDescription,
    var value: Double,
    var level: MetricLevel,
    var tests: List<TestData>, // for single test metric will store only one item
)