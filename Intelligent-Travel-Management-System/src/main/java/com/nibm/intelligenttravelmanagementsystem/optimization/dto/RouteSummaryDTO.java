package com.nibm.intelligenttravelmanagementsystem.optimization.dto;

import com.nibm.intelligenttravelmanagementsystem.optimization.model.TravelEdge;
import java.util.List;

public class RouteSummaryDTO {
    private String label;
    private List<String> pathNodeIds;
    private List<String> pathNodeNames;
    private List<TravelEdge> edges;

    private double totalDistanceKm;
    private double totalDurationMinutes;
    private int totalCostLkr;
    private double averageRiskLevel;
    private double averageRoadQuality;
    private double compositeScore;

    public RouteSummaryDTO() {}

    public static RouteSummaryDTOBuilder builder() { return new RouteSummaryDTOBuilder(); }

    public static class RouteSummaryDTOBuilder {
        private String label;
        private List<String> pathNodeIds;
        private List<String> pathNodeNames;
        private List<TravelEdge> edges;
        private double totalDistanceKm;
        private double totalDurationMinutes;
        private int totalCostLkr;
        private double averageRiskLevel;
        private double averageRoadQuality;
        private double compositeScore;

        public RouteSummaryDTOBuilder label(String l) { this.label = l; return this; }
        public RouteSummaryDTOBuilder pathNodeIds(List<String> ids) { this.pathNodeIds = ids; return this; }
        public RouteSummaryDTOBuilder pathNodeNames(List<String> names) { this.pathNodeNames = names; return this; }
        public RouteSummaryDTOBuilder edges(List<TravelEdge> edges) { this.edges = edges; return this; }
        public RouteSummaryDTOBuilder totalDistanceKm(double d) { this.totalDistanceKm = d; return this; }
        public RouteSummaryDTOBuilder totalDurationMinutes(double t) { this.totalDurationMinutes = t; return this; }
        public RouteSummaryDTOBuilder totalCostLkr(int c) { this.totalCostLkr = c; return this; }
        public RouteSummaryDTOBuilder averageRiskLevel(double r) { this.averageRiskLevel = r; return this; }
        public RouteSummaryDTOBuilder averageRoadQuality(double q) { this.averageRoadQuality = q; return this; }
        public RouteSummaryDTOBuilder compositeScore(double s) { this.compositeScore = s; return this; }

        public RouteSummaryDTO build() {
            RouteSummaryDTO dto = new RouteSummaryDTO();
            dto.label = this.label;
            dto.pathNodeIds = this.pathNodeIds;
            dto.pathNodeNames = this.pathNodeNames;
            dto.edges = this.edges;
            dto.totalDistanceKm = this.totalDistanceKm;
            dto.totalDurationMinutes = this.totalDurationMinutes;
            dto.totalCostLkr = this.totalCostLkr;
            dto.averageRiskLevel = this.averageRiskLevel;
            dto.averageRoadQuality = this.averageRoadQuality;
            dto.compositeScore = this.compositeScore;
            return dto;
        }
    }

    public String getLabel() { return label; }
    public List<String> getPathNodeIds() { return pathNodeIds; }
    public List<String> getPathNodeNames() { return pathNodeNames; }
    public List<TravelEdge> getEdges() { return edges; }
    public double getTotalDistanceKm() { return totalDistanceKm; }
    public double getTotalDurationMinutes() { return totalDurationMinutes; }
    public int getTotalCostLkr() { return totalCostLkr; }
    public double getAverageRiskLevel() { return averageRiskLevel; }
    public double getAverageRoadQuality() { return averageRoadQuality; }
    public double getCompositeScore() { return compositeScore; }
}
