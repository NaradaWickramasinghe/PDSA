package com.nibm.intelligenttravelmanagementsystem.optimization.dto;

import java.util.Map;

public class BenchmarkResponse {
    private String scenarioName;
    private int networkNodesCount;
    private int networkEdgesCount;
    private String sourceNodeId;
    private String destinationNodeId;
    private Map<String, AlgorithmMetricDTO> algorithmMetrics;

    public BenchmarkResponse() {}

    public static BenchmarkResponseBuilder builder() { return new BenchmarkResponseBuilder(); }

    public static class BenchmarkResponseBuilder {
        private String scenarioName;
        private int networkNodesCount;
        private int networkEdgesCount;
        private String sourceNodeId;
        private String destinationNodeId;
        private Map<String, AlgorithmMetricDTO> algorithmMetrics;

        public BenchmarkResponseBuilder scenarioName(String s) { this.scenarioName = s; return this; }
        public BenchmarkResponseBuilder networkNodesCount(int n) { this.networkNodesCount = n; return this; }
        public BenchmarkResponseBuilder networkEdgesCount(int e) { this.networkEdgesCount = e; return this; }
        public BenchmarkResponseBuilder sourceNodeId(String src) { this.sourceNodeId = src; return this; }
        public BenchmarkResponseBuilder destinationNodeId(String dst) { this.destinationNodeId = dst; return this; }
        public BenchmarkResponseBuilder algorithmMetrics(Map<String, AlgorithmMetricDTO> m) { this.algorithmMetrics = m; return this; }

        public BenchmarkResponse build() {
            BenchmarkResponse r = new BenchmarkResponse();
            r.scenarioName = this.scenarioName;
            r.networkNodesCount = this.networkNodesCount;
            r.networkEdgesCount = this.networkEdgesCount;
            r.sourceNodeId = this.sourceNodeId;
            r.destinationNodeId = this.destinationNodeId;
            r.algorithmMetrics = this.algorithmMetrics;
            return r;
        }
    }

    public static class AlgorithmMetricDTO {
        private String algorithmName;
        private double executionTimeMs;
        private double memoryUsedKb;
        private double bestCompositeScore;
        private double totalDistanceKm;
        private double totalDurationMinutes;
        private int totalCostLkr;
        private double averageRiskLevel;
        private int nodesExplored;
        private boolean foundValidPath;

        public AlgorithmMetricDTO() {}

        public static AlgorithmMetricDTOBuilder builder() { return new AlgorithmMetricDTOBuilder(); }

        public static class AlgorithmMetricDTOBuilder {
            private String algorithmName;
            private double executionTimeMs;
            private double memoryUsedKb;
            private double bestCompositeScore;
            private double totalDistanceKm;
            private double totalDurationMinutes;
            private int totalCostLkr;
            private double averageRiskLevel;
            private int nodesExplored;
            private boolean foundValidPath;

            public AlgorithmMetricDTOBuilder algorithmName(String a) { this.algorithmName = a; return this; }
            public AlgorithmMetricDTOBuilder executionTimeMs(double t) { this.executionTimeMs = t; return this; }
            public AlgorithmMetricDTOBuilder memoryUsedKb(double m) { this.memoryUsedKb = m; return this; }
            public AlgorithmMetricDTOBuilder bestCompositeScore(double s) { this.bestCompositeScore = s; return this; }
            public AlgorithmMetricDTOBuilder totalDistanceKm(double d) { this.totalDistanceKm = d; return this; }
            public AlgorithmMetricDTOBuilder totalDurationMinutes(double dur) { this.totalDurationMinutes = dur; return this; }
            public AlgorithmMetricDTOBuilder totalCostLkr(int c) { this.totalCostLkr = c; return this; }
            public AlgorithmMetricDTOBuilder averageRiskLevel(double r) { this.averageRiskLevel = r; return this; }
            public AlgorithmMetricDTOBuilder nodesExplored(int n) { this.nodesExplored = n; return this; }
            public AlgorithmMetricDTOBuilder foundValidPath(boolean f) { this.foundValidPath = f; return this; }

            public AlgorithmMetricDTO build() {
                AlgorithmMetricDTO dto = new AlgorithmMetricDTO();
                dto.algorithmName = this.algorithmName;
                dto.executionTimeMs = this.executionTimeMs;
                dto.memoryUsedKb = this.memoryUsedKb;
                dto.bestCompositeScore = this.bestCompositeScore;
                dto.totalDistanceKm = this.totalDistanceKm;
                dto.totalDurationMinutes = this.totalDurationMinutes;
                dto.totalCostLkr = this.totalCostLkr;
                dto.averageRiskLevel = this.averageRiskLevel;
                dto.nodesExplored = this.nodesExplored;
                dto.foundValidPath = this.foundValidPath;
                return dto;
            }
        }

        public String getAlgorithmName() { return algorithmName; }
        public double getExecutionTimeMs() { return executionTimeMs; }
        public double getMemoryUsedKb() { return memoryUsedKb; }
        public double getBestCompositeScore() { return bestCompositeScore; }
        public double getTotalDistanceKm() { return totalDistanceKm; }
        public double getTotalDurationMinutes() { return totalDurationMinutes; }
        public int getTotalCostLkr() { return totalCostLkr; }
        public double getAverageRiskLevel() { return averageRiskLevel; }
        public int getNodesExplored() { return nodesExplored; }
        public boolean isFoundValidPath() { return foundValidPath; }
    }

    public String getScenarioName() { return scenarioName; }
    public int getNetworkNodesCount() { return networkNodesCount; }
    public int getNetworkEdgesCount() { return networkEdgesCount; }
    public String getSourceNodeId() { return sourceNodeId; }
    public String getDestinationNodeId() { return destinationNodeId; }
    public Map<String, AlgorithmMetricDTO> getAlgorithmMetrics() { return algorithmMetrics; }
}
