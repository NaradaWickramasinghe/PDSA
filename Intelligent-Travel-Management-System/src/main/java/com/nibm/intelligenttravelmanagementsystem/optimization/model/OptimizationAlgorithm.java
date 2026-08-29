package com.nibm.intelligenttravelmanagementsystem.optimization.model;

import com.nibm.intelligenttravelmanagementsystem.optimization.dto.OptimizationRequest;

public interface OptimizationAlgorithm {
    String getName();
    OptimizationResult optimize(TravelGraph graph, OptimizationRequest request);
}
