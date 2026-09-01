package com.nibm.intelligenttravelmanagementsystem.resourceallocation.repository;

import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.ResourceCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Entity
@Table(name = "resource_options")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceOptionEntity {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @Column(name = "destination", length = 64, nullable = false)
    private String destination;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 32)
    private ResourceCategory category;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @JdbcTypeCode(SqlTypes.NUMERIC)
    @Column(name = "cost", nullable = false)
    private double cost;

    @JdbcTypeCode(SqlTypes.NUMERIC)
    @Column(name = "duration_hours", nullable = false)
    private double durationHours;

    @JdbcTypeCode(SqlTypes.NUMERIC)
    @Column(name = "weight_kg", nullable = false)
    private double weightKg;

    @JdbcTypeCode(SqlTypes.NUMERIC)
    @Column(name = "usefulness", nullable = false)
    private double usefulness;

    @Column(name = "available", nullable = false)
    @Builder.Default
    private boolean available = true;

    @Column(name = "transport_type", length = 32)
    private String transportType;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "location_node_id")
    private Long locationNodeId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;
}
