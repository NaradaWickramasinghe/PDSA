package com.nibm.intelligenttravelmanagementsystem.optimization.model;

import java.util.ArrayList;
import java.util.List;

public class RouteSolution {
    private final List<String> route;
    private final double totalDistance;
    private final double totalTravelTime;
    private final double totalCost;
    private final double objectiveScore;
    private final long executionTimeMs;
    private final int nodesExplored;
    private final int statesExplored;
    private final boolean success;
    private final String errorMessage;

    public RouteSolution(List<String> route, double totalDistance, double totalTravelTime, double totalCost,
            double objectiveScore, long executionTimeMs, int nodesExplored,
            int statesExplored, boolean success, String errorMessage) {
        this.route = new ArrayList<>(route);
        this.totalDistance = totalDistance;
        this.totalTravelTime = totalTravelTime;
        this.totalCost = totalCost;
        this.objectiveScore = objectiveScore;
        this.executionTimeMs = executionTimeMs;
        this.nodesExplored = nodesExplored;
        this.statesExplored = statesExplored;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public List<String> getRoute() {
        return route;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public double getTotalTravelTime() {
        return totalTravelTime;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public double getObjectiveScore() {
        return objectiveScore;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public int getNodesExplored() {
        return nodesExplored;
    }

    public int getStatesExplored() {
        return statesExplored;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
