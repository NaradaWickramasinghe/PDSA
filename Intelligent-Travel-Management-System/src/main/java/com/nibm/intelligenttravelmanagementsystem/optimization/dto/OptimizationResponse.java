package com.nibm.intelligenttravelmanagementsystem.optimization.dto;

import java.util.List;

public class OptimizationResponse {
    private boolean success;
    private String message;
    private String selectedAlgorithm;
    private String sourceNodeId;
    private String destinationNodeId;

    private RouteSummaryDTO bestRoute;
    private List<RouteSummaryDTO> paretoAlternatives;

    private double executionTimeMs;
    private double memoryUsedKb;
    private int nodesExploredCount;

    public OptimizationResponse() {}

    public static OptimizationResponseBuilder builder() { return new OptimizationResponseBuilder(); }

    public static class OptimizationResponseBuilder {
        private boolean success;
        private String message;
        private String selectedAlgorithm;
        private String sourceNodeId;
        private String destinationNodeId;
        private RouteSummaryDTO bestRoute;
        private List<RouteSummaryDTO> paretoAlternatives;
        private double executionTimeMs;
        private double memoryUsedKb;
        private int nodesExploredCount;

        public OptimizationResponseBuilder success(boolean s) { this.success = s; return this; }
        public OptimizationResponseBuilder message(String m) { this.message = m; return this; }
        public OptimizationResponseBuilder selectedAlgorithm(String a) { this.selectedAlgorithm = a; return this; }
        public OptimizationResponseBuilder sourceNodeId(String src) { this.sourceNodeId = src; return this; }
        public OptimizationResponseBuilder destinationNodeId(String dst) { this.destinationNodeId = dst; return this; }
        public OptimizationResponseBuilder bestRoute(RouteSummaryDTO r) { this.bestRoute = r; return this; }
        public OptimizationResponseBuilder paretoAlternatives(List<RouteSummaryDTO> p) { this.paretoAlternatives = p; return this; }
        public OptimizationResponseBuilder executionTimeMs(double t) { this.executionTimeMs = t; return this; }
        public OptimizationResponseBuilder memoryUsedKb(double m) { this.memoryUsedKb = m; return this; }
        public OptimizationResponseBuilder nodesExploredCount(int n) { this.nodesExploredCount = n; return this; }

        public OptimizationResponse build() {
            OptimizationResponse res = new OptimizationResponse();
            res.success = this.success;
            res.message = this.message;
            res.selectedAlgorithm = this.selectedAlgorithm;
            res.sourceNodeId = this.sourceNodeId;
            res.destinationNodeId = this.destinationNodeId;
            res.bestRoute = this.bestRoute;
            res.paretoAlternatives = this.paretoAlternatives;
            res.executionTimeMs = this.executionTimeMs;
            res.memoryUsedKb = this.memoryUsedKb;
            res.nodesExploredCount = this.nodesExploredCount;
            return res;
        }
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getSelectedAlgorithm() { return selectedAlgorithm; }
    public String getSourceNodeId() { return sourceNodeId; }
    public String getDestinationNodeId() { return destinationNodeId; }
    public RouteSummaryDTO getBestRoute() { return bestRoute; }
    public List<RouteSummaryDTO> getParetoAlternatives() { return paretoAlternatives; }
    public double getExecutionTimeMs() { return executionTimeMs; }
    public double getMemoryUsedKb() { return memoryUsedKb; }
    public int getNodesExploredCount() { return nodesExploredCount; }
}
