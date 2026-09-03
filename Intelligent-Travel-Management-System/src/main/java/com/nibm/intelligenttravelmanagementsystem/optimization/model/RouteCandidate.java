package com.nibm.intelligenttravelmanagementsystem.optimization.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RouteCandidate implements Comparable<RouteCandidate> {
    private List<String> nodeIds;
    private List<TravelEdge> edges;
    private Set<String> visitedSet;

    private double totalDistanceKm;
    private double totalDurationMinutes;
    private int totalCostLkr;
    private double sumRisk;
    private double sumRoadQuality;
    private int maxRiskObserved;
    private double compositeScore;

    public RouteCandidate(String startNodeId) {
        this.nodeIds = new ArrayList<>();
        this.nodeIds.add(startNodeId);
        this.edges = new ArrayList<>();
        this.visitedSet = new HashSet<>();
        this.visitedSet.add(startNodeId);

        this.totalDistanceKm = 0.0;
        this.totalDurationMinutes = 0.0;
        this.totalCostLkr = 0;
        this.sumRisk = 0.0;
        this.sumRoadQuality = 0.0;
        this.maxRiskObserved = 1;
        this.compositeScore = 0.0;
    }

    private RouteCandidate() {
        this.nodeIds = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.visitedSet = new HashSet<>();
    }

    public void addStep(TravelEdge edge) {
        this.edges.add(edge);
        this.nodeIds.add(edge.getDestination());
        this.visitedSet.add(edge.getDestination());

        this.totalDistanceKm += edge.getDistanceKm();
        this.totalDurationMinutes += edge.getEffectiveTravelTime();
        this.totalCostLkr += edge.getEstimatedCostLkr();
        this.sumRisk += edge.getRiskLevel();
        this.sumRoadQuality += edge.getRoadQuality();
        if (edge.getRiskLevel() > this.maxRiskObserved) {
            this.maxRiskObserved = edge.getRiskLevel();
        }
    }

    public boolean containsNode(String nodeId) {
        return this.visitedSet.contains(nodeId);
    }

    public String getCurrentNodeId() {
        if (nodeIds.isEmpty()) return null;
        return nodeIds.get(nodeIds.size() - 1);
    }

    public double getAverageRiskLevel() {
        if (edges.isEmpty()) return 1.0;
        return sumRisk / edges.size();
    }

    public double getAverageRoadQuality() {
        if (edges.isEmpty()) return 5.0;
        return sumRoadQuality / edges.size();
    }

    public RouteCandidate deepCopy() {
        RouteCandidate copy = new RouteCandidate();
        copy.nodeIds = new ArrayList<>(this.nodeIds);
        copy.edges = new ArrayList<>(this.edges);
        copy.visitedSet = new HashSet<>(this.visitedSet);
        copy.totalDistanceKm = this.totalDistanceKm;
        copy.totalDurationMinutes = this.totalDurationMinutes;
        copy.totalCostLkr = this.totalCostLkr;
        copy.sumRisk = this.sumRisk;
        copy.sumRoadQuality = this.sumRoadQuality;
        copy.maxRiskObserved = this.maxRiskObserved;
        copy.compositeScore = this.compositeScore;
        return copy;
    }

    public boolean dominates(RouteCandidate other) {
        boolean betterOrEqual = this.totalDurationMinutes <= other.totalDurationMinutes
                && this.totalCostLkr <= other.totalCostLkr
                && this.getAverageRiskLevel() <= other.getAverageRiskLevel()
                && this.getAverageRoadQuality() >= other.getAverageRoadQuality();

        boolean strictlyBetter = this.totalDurationMinutes < other.totalDurationMinutes
                || this.totalCostLkr < other.totalCostLkr
                || this.getAverageRiskLevel() < other.getAverageRiskLevel()
                || this.getAverageRoadQuality() > other.getAverageRoadQuality();

        return betterOrEqual && strictlyBetter;
    }

    @Override
    public int compareTo(RouteCandidate other) {
        return Double.compare(this.compositeScore, other.compositeScore);
    }

    // Getters and Setters
    public List<String> getNodeIds() { return nodeIds; }
    public void setNodeIds(List<String> nodeIds) { this.nodeIds = nodeIds; }

    public List<TravelEdge> getEdges() { return edges; }
    public void setEdges(List<TravelEdge> edges) { this.edges = edges; }

    public double getTotalDistanceKm() { return totalDistanceKm; }
    public void setTotalDistanceKm(double totalDistanceKm) { this.totalDistanceKm = totalDistanceKm; }

    public double getTotalDurationMinutes() { return totalDurationMinutes; }
    public void setTotalDurationMinutes(double totalDurationMinutes) { this.totalDurationMinutes = totalDurationMinutes; }

    public int getTotalCostLkr() { return totalCostLkr; }
    public void setTotalCostLkr(int totalCostLkr) { this.totalCostLkr = totalCostLkr; }

    public int getMaxRiskObserved() { return maxRiskObserved; }
    public void setMaxRiskObserved(int maxRiskObserved) { this.maxRiskObserved = maxRiskObserved; }

    public double getCompositeScore() { return compositeScore; }
    public void setCompositeScore(double compositeScore) { this.compositeScore = compositeScore; }
}
