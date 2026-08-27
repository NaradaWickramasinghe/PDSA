package com.nibm.intelligenttravelmanagementsystem.shared.db.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "edges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Edge {

    @Id
    @Column(name = "edge_id", nullable = false, unique = true)
    private String edgeId;

    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "destination", nullable = false)
    private String destination;

    @Column(name = "distance_km", nullable = false)
    private Double distanceKm;

    @Column(name = "travel_time_minutes", nullable = false)
    private Integer travelTimeMinutes;

    @Column(name = "estimated_cost_lkr", nullable = false)
    private Integer estimatedCostLkr;

    @Column(name = "road_quality", nullable = false)
    private Integer roadQuality;

    @Column(name = "traffic_level", nullable = false)
    private Integer trafficLevel;

    @Column(name = "transport_mode", nullable = false)
    private String transportMode;

    @Column(name = "accessibility", nullable = false)
    private Integer accessibility;

    @Column(name = "risk_level", nullable = false)
    private Integer riskLevel;

    @Column(name = "id", insertable = false, updatable = false)
    private Long id;

    @Column(name = "distance")
    private Double distance;

    @Column(name = "source_node_id")
    private Long sourceNodeId;

    @Column(name = "target_node_id")
    private Long targetNodeId;

    @Column(name = "travel_time")
    private Double travelTime;
}