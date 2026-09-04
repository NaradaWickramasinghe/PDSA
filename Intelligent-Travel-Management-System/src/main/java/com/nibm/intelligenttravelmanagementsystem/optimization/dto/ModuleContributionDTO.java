package com.nibm.intelligenttravelmanagementsystem.optimization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing the synthesis of outputs from Modules 1, 2, 3, and 4
 * for a single destination or checkpoint in the overall optimization plan.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleContributionDTO {

    private String destinationId;
    private String destinationName;

    // Module 4 output: Intelligent Decision & Machine Learning recommendation
    private String module4Suitability;       // EXCELLENT, SUITABLE, MODERATE
    private double module4MatchScore;        // Preference & KNN match score [0.0 - 1.0]

    // Module 3 output: Network Analysis & Graph Topology
    private double module3Betweenness;       // Brandes Betweenness Centrality (hub importance)
    private double module3Closeness;         // Brandes Closeness Centrality (emergency reach)

    // Module 1 output: Route Optimization (Dijkstra / A*)
    private double module1DistanceKm;        // Real shortest path distance from previous stop
    private double module1DurationMinutes;   // Real travel duration considering traffic
    private int module1RiskLevel;            // Road/trail safety risk rating [1 - 5]

    // Module 2 output: Resource Allocation
    private String module2AllocatedPackage;  // Assigned resources/gear package
    private double module2ResourceCostLkr;   // Equipment, transport, and guide cost

    // Module 5 combined score
    private double compositeUtilityScore;    // Multi-objective balanced utility
}
