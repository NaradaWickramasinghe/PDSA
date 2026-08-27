package com.nibm.intelligenttravelmanagementsystem.optimization.model;

import com.nibm.intelligenttravelmanagementsystem.optimization.dto.OptimizationRequest;

public class CostEvaluator {
    private static final double BASE_TIME_MINUTES = 180.0;
    private static final double BASE_COST_LKR = 5000.0;

    public static double computeCompositeScore(RouteCandidate route, OptimizationRequest request) {
        request.normalizeWeights();

        double normalizedTime = route.getTotalDurationMinutes() / BASE_TIME_MINUTES;
        double normalizedCost = (double) route.getTotalCostLkr() / BASE_COST_LKR;
        double normalizedRisk = route.getAverageRiskLevel() / 5.0;
        double normalizedQuality = (6.0 - route.getAverageRoadQuality()) / 5.0;

        double score = (request.getTimeWeight() * normalizedTime)
                + (request.getCostWeight() * normalizedCost)
                + (request.getSafetyWeight() * normalizedRisk)
                + (request.getQualityWeight() * normalizedQuality);

        route.setCompositeScore(score);
        return score;
    }

    public static boolean satisfiesConstraints(RouteCandidate route, OptimizationRequest request) {
        if (request.getMaxTimeMinutes() != null && route.getTotalDurationMinutes() > request.getMaxTimeMinutes()) {
            return false;
        }
        if (request.getMaxBudgetLkr() != null && route.getTotalCostLkr() > request.getMaxBudgetLkr()) {
            return false;
        }
        if (request.getMaxAllowedRisk() != null && route.getMaxRiskObserved() > request.getMaxAllowedRisk()) {
            return false;
        }
        return true;
    }
}
