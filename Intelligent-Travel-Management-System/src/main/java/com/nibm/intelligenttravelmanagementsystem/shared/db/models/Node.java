package com.nibm.intelligenttravelmanagementsystem.shared.db.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "nodes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Node {

    @Id
    @Column(name = "node_id", nullable = false, unique = true)
    private String nodeId;

    @Column(name = "id", insertable = false, updatable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "node_type")
    private String nodeType;

    @Column(name = "province")
    private String province;

    @Column(name = "district")
    private String district;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}