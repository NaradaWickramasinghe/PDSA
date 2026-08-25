package com.nibm.intelligenttravelmanagementsystem.shared.db.models;

import jakarta.persistence.*;

@Entity
@Table(name = "nodes")
public class Node {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double latitude;
    private Double longitude;

    private String name;
}