package com.tuc.masters.core.models

//TODO: add toString implementation as requirement
data class MetricDescription (
    var name: String,
    var description: String,
    var availableLevels: List<MetricLevel>,
    var artifactTypes: List<ArtifactType>
)
