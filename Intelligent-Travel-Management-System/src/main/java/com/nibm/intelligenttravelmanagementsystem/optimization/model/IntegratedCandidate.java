package com.nibm.intelligenttravelmanagementsystem.optimization.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Encapsulates a candidate destination stop with metrics collected
 * from Modules 1, 2, 3, and 4 before optimization algorithms run.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegratedCandidate {

    private String nodeId;
    private String name;
    private String province;
    private String district;
    private double latitude;
    private double longitude;

    // Module 4 inputs
    private String decisionSuitability;  // EXCELLENT, SUITABLE, etc.
    private double matchScore;           // [0.0 - 1.0]

    // Module 3 inputs
    private double betweennessCentrality;// Hub significance
    private double closenessCentrality;  // Reachability/emergency response

    // Module 1 inputs
    private double transitDistanceKm;    // Shortest path distance
    private double transitDurationMinutes;// Estimated road/trail time
    private int riskLevel;               // Segment hazard rating [1 - 5]

    // Module 2 inputs
    private double resourceCostLkr;      // Gear, transport, guide cost
    private String allocatedPackage;     // Selected equipment bundle
    private boolean resourceFeasible;    // Feasibility status

    // Synthesized Optimization Weight & Value
    private double compositeValue;       // Total utility value (for Knapsack / Branch & Bound)
    private double compositeCost;        // Total cost (resource cost + transit cost)
    private double compositeTimeMinutes; // Transit duration + stop stay duration

    /**
     * Calculates the overall utility score balancing preference alignment and network hub safety.
     */
    public void computeCompositeMetrics(double prefWeight, double safetyWeight) {
        // Base value from Module 4 (Decision Tree / KNN match)
        double decisionVal = Math.max(0.1, matchScore);
        // Safety & resilience bonus from Module 3 Centrality
        double centralityVal = Math.max(0.05, betweennessCentrality + closenessCentrality);

        this.compositeValue = (prefWeight * decisionVal) + (safetyWeight * centralityVal);
        // Cost combines Module 2 equipment with estimated transit fuel/fares
        this.compositeCost = resourceCostLkr + (transitDistanceKm * 65.0);
        // Time includes transit duration plus typical stop duration (e.g. 90 minutes)
        this.compositeTimeMinutes = transitDurationMinutes + 90.0;
    }
}
