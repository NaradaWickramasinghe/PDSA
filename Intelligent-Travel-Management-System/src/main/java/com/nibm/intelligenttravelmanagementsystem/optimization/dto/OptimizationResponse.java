package com.nibm.intelligenttravelmanagementsystem.optimization.dto;

import java.util.List;

public class OptimizationResponse {
    private String selectedAlgorithm;
    private List<String> route;
    private double totalDistance;
    private double totalTravelTime;
    private double totalCost;
    private double objectiveScore;
    private long executionTimeMs;
    private int nodesExplored;
    private int statesExplored;
    private boolean success;
    private String errorMessage;

    public OptimizationResponse() {
    }

    public OptimizationResponse(String selectedAlgorithm, List<String> route, double totalDistance,
            double totalTravelTime, double totalCost, double objectiveScore,
            long executionTimeMs, int nodesExplored, int statesExplored,
            boolean success, String errorMessage) {
        this.selectedAlgorithm = selectedAlgorithm;
        this.route = route;
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

    public String getSelectedAlgorithm() {
        return selectedAlgorithm;
    }

    public void setSelectedAlgorithm(String selectedAlgorithm) {
        this.selectedAlgorithm = selectedAlgorithm;
    }

    public List<String> getRoute() {
        return route;
    }

    public void setRoute(List<String> route) {
        this.route = route;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public double getTotalTravelTime() {
        return totalTravelTime;
    }

    public void setTotalTravelTime(double totalTravelTime) {
        this.totalTravelTime = totalTravelTime;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public double getObjectiveScore() {
        return objectiveScore;
    }

    public void setObjectiveScore(double objectiveScore) {
        this.objectiveScore = objectiveScore;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public int getNodesExplored() {
        return nodesExplored;
    }

    public void setNodesExplored(int nodesExplored) {
        this.nodesExplored = nodesExplored;
    }

    public int getStatesExplored() {
        return statesExplored;
    }

    public void setStatesExplored(int statesExplored) {
        this.statesExplored = statesExplored;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
