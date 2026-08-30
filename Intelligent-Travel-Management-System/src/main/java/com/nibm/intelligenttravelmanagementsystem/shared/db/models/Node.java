package com.nibm.intelligenttravelmanagementsystem.shared.db.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "nodes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Node {

    @Id
    @Column(name = "node_id", length = 20)
    private String nodeId;

    @Column(name = "id", insertable = false, updatable = false)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "node_type", nullable = false, length = 30)
    private String nodeType;

    @Column(nullable = false, length = 50)
    private String province;

    @Column(nullable = false, length = 50)
    private String district;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(columnDefinition = "TEXT")
    private String description;
}