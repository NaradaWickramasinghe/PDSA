package com.nibm.intelligenttravelmanagementsystem.optimization.model;

import java.util.ArrayList;
import java.util.List;

public class OptimizationResult {
    private String algorithmName;
    private RouteCandidate bestRoute;
    private List<RouteCandidate> paretoAlternatives = new ArrayList<>();
    private double executionTimeMs;
    private double memoryUsedKb;
    private int nodesExplored;
    private boolean success;
    private String message;

    public OptimizationResult() {}

    public static OptimizationResultBuilder builder() { return new OptimizationResultBuilder(); }

    public static class OptimizationResultBuilder {
        private String algorithmName;
        private RouteCandidate bestRoute;
        private List<RouteCandidate> paretoAlternatives = new ArrayList<>();
        private double executionTimeMs;
        private double memoryUsedKb;
        private int nodesExplored;
        private boolean success;
        private String message;

        public OptimizationResultBuilder algorithmName(String name) { this.algorithmName = name; return this; }
        public OptimizationResultBuilder bestRoute(RouteCandidate route) { this.bestRoute = route; return this; }
        public OptimizationResultBuilder paretoAlternatives(List<RouteCandidate> alts) { this.paretoAlternatives = alts; return this; }
        public OptimizationResultBuilder executionTimeMs(double time) { this.executionTimeMs = time; return this; }
        public OptimizationResultBuilder memoryUsedKb(double mem) { this.memoryUsedKb = mem; return this; }
        public OptimizationResultBuilder nodesExplored(int count) { this.nodesExplored = count; return this; }
        public OptimizationResultBuilder success(boolean success) { this.success = success; return this; }
        public OptimizationResultBuilder message(String msg) { this.message = msg; return this; }

        public OptimizationResult build() {
            OptimizationResult res = new OptimizationResult();
            res.algorithmName = this.algorithmName;
            res.bestRoute = this.bestRoute;
            res.paretoAlternatives = this.paretoAlternatives;
            res.executionTimeMs = this.executionTimeMs;
            res.memoryUsedKb = this.memoryUsedKb;
            res.nodesExplored = this.nodesExplored;
            res.success = this.success;
            res.message = this.message;
            return res;
        }
    }

    public String getAlgorithmName() { return algorithmName; }
    public RouteCandidate getBestRoute() { return bestRoute; }
    public List<RouteCandidate> getParetoAlternatives() { return paretoAlternatives; }
    public double getExecutionTimeMs() { return executionTimeMs; }
    public double getMemoryUsedKb() { return memoryUsedKb; }
    public int getNodesExplored() { return nodesExplored; }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}
