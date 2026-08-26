package com.nibm.intelligenttravelmanagementsystem.shared.db.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "edges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Edge {

    @Id
    @Column(name = "edge_id", length = 20)
    private String edgeId;

    @Column(nullable = false, length = 20)
    private String source;

    @Column(nullable = false, length = 20)
    private String destination;

    @Column(name = "distance_km", nullable = false)
    private Double distanceKm;

    @Column(name = "travel_time_minutes", nullable = false)
    private Integer travelTimeMinutes;

    @Column(name = "estimated_cost_lkr", nullable = false)
    private Integer estimatedCostLkr;

    @Column(name = "road_quality", nullable = false)
    private Short roadQuality;

    @Column(name = "traffic_level", nullable = false)
    private Short trafficLevel;

    @Column(name = "transport_mode", nullable = false, length = 20)
    private String transportMode;

    @Column(nullable = false)
    private Short accessibility;

    @Column(name = "risk_level", nullable = false)
    private Short riskLevel;
}