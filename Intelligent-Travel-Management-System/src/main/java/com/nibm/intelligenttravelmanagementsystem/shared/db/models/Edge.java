package com.nibm.intelligenttravelmanagementsystem.shared.db.models;

import jakarta.persistence.*;

@Entity
@Table(name = "edges")
public class Edge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sourceNodeId;
    private Long targetNodeId;

    private String sourceName;
    private String targetName;
    private Double distance;
    private Double travelTime;
    private Double cost;

    public Edge() {
    }

    public Edge(String sourceName, String targetName, Double distance, Double travelTime, Double cost) {
        this.sourceName = sourceName;
        this.targetName = targetName;
        this.distance = distance;
        this.travelTime = travelTime;
        this.cost = cost;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSourceNodeId() {
        return sourceNodeId;
    }

    public void setSourceNodeId(Long sourceNodeId) {
        this.sourceNodeId = sourceNodeId;
    }

    public Long getTargetNodeId() {
        return targetNodeId;
    }

    public void setTargetNodeId(Long targetNodeId) {
        this.targetNodeId = targetNodeId;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Double getTravelTime() {
        return travelTime;
    }

    public void setTravelTime(Double travelTime) {
        this.travelTime = travelTime;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }
}