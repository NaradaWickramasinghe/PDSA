package com.nibm.intelligenttravelmanagementsystem.optimization.dto;

import java.util.List;

public class BenchmarkSummary {
    private List<OptimizationResponse> results;
    private String bestAlgorithm;
    private double bestObjectiveScore;

    public BenchmarkSummary() {
    }

    public BenchmarkSummary(List<OptimizationResponse> results, String bestAlgorithm, double bestObjectiveScore) {
        this.results = results;
        this.bestAlgorithm = bestAlgorithm;
        this.bestObjectiveScore = bestObjectiveScore;
    }

    public List<OptimizationResponse> getResults() {
        return results;
    }

    public void setResults(List<OptimizationResponse> results) {
        this.results = results;
    }

    public String getBestAlgorithm() {
        return bestAlgorithm;
    }

    public void setBestAlgorithm(String bestAlgorithm) {
        this.bestAlgorithm = bestAlgorithm;
    }

    public double getBestObjectiveScore() {
        return bestObjectiveScore;
    }

    public void setBestObjectiveScore(double bestObjectiveScore) {
        this.bestObjectiveScore = bestObjectiveScore;
    }
}
