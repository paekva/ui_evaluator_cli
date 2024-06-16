package com.tuc.masters.core.models

// Description of the implemented metric
data class MetricDescription (
    var name: String,
    var description: String,
    var availableLevels: List<MetricLevel>,
    var artifactTypes: List<ArtifactType>
)
