package com.nibm.intelligenttravelmanagementsystem.optimization.dto;

public class OptimizationRequest {
    private String origin;
    private String destination;
    private Double maxTravelTime;
    private Double maxBudget;
    private String optimizationMethod;
    private ObjectiveWeights objectiveWeights;

    public OptimizationRequest() {
    }

    public OptimizationRequest(String origin, String destination, Double maxTravelTime, Double maxBudget,
            String optimizationMethod, ObjectiveWeights objectiveWeights) {
        this.origin = origin;
        this.destination = destination;
        this.maxTravelTime = maxTravelTime;
        this.maxBudget = maxBudget;
        this.optimizationMethod = optimizationMethod;
        this.objectiveWeights = objectiveWeights;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Double getMaxTravelTime() {
        return maxTravelTime;
    }

    public void setMaxTravelTime(Double maxTravelTime) {
        this.maxTravelTime = maxTravelTime;
    }

    public Double getMaxBudget() {
        return maxBudget;
    }

    public void setMaxBudget(Double maxBudget) {
        this.maxBudget = maxBudget;
    }

    public String getOptimizationMethod() {
        return optimizationMethod;
    }

    public void setOptimizationMethod(String optimizationMethod) {
        this.optimizationMethod = optimizationMethod;
    }

    public ObjectiveWeights getObjectiveWeights() {
        return objectiveWeights;
    }

    public void setObjectiveWeights(ObjectiveWeights objectiveWeights) {
        this.objectiveWeights = objectiveWeights;
    }
}
