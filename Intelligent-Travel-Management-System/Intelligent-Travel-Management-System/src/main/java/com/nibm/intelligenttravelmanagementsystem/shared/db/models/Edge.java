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

    private Double distance;
    private Double travelTime;
}