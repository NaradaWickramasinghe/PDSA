package com.nibm.intelligenttravelmanagementsystem.resourceallocation.repository;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "allocation_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocationHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "algorithm_used", nullable = false, length = 32)
    private String algorithmUsed;

    @Column(name = "feasible", nullable = false)
    private boolean feasible;

    @Column(name = "destination", length = 64)
    private String destination;

    @Column(name = "trip_duration_days")
    private Integer tripDurationDays;

    @Column(name = "traveller_count")
    private Integer travellerCount;

    @JdbcTypeCode(SqlTypes.NUMERIC)
    @Column(name = "total_budget", nullable = false)
    private double totalBudget;

    @JdbcTypeCode(SqlTypes.NUMERIC)
    @Column(name = "emergency_reserve", nullable = false)
    private double emergencyReserve;

    @JdbcTypeCode(SqlTypes.NUMERIC)
    @Column(name = "max_available_hours", nullable = false)
    private double maxAvailableHours;

    @JdbcTypeCode(SqlTypes.NUMERIC)
    @Column(name = "max_capacity_kg", nullable = false)
    private double maxCapacityKg;

    @JdbcTypeCode(SqlTypes.NUMERIC)
    @Column(name = "total_cost", nullable = false)
    private double totalCost;

    @JdbcTypeCode(SqlTypes.NUMERIC)
    @Column(name = "remaining_budget", nullable = false)
    private double remainingBudget;

    @JdbcTypeCode(SqlTypes.NUMERIC)
    @Column(name = "total_time_used", nullable = false)
    private double totalTimeUsed;

    @JdbcTypeCode(SqlTypes.NUMERIC)
    @Column(name = "remaining_time", nullable = false)
    private double remainingTime;

    @JdbcTypeCode(SqlTypes.NUMERIC)
    @Column(name = "total_weight", nullable = false)
    private double totalWeight;

    @JdbcTypeCode(SqlTypes.NUMERIC)
    @Column(name = "remaining_capacity", nullable = false)
    private double remainingCapacity;

    @JdbcTypeCode(SqlTypes.NUMERIC)
    @Column(name = "overall_score", nullable = false)
    private double overallScore;

    @Column(name = "execution_time_ms", nullable = false)
    private long executionTimeMs;

    @Column(name = "status_message", columnDefinition = "TEXT")
    private String statusMessage;

    @ManyToMany
    @JoinTable(
            name = "allocation_history_items",
            joinColumns = @JoinColumn(name = "allocation_id"),
            inverseJoinColumns = @JoinColumn(name = "resource_id")
    )
    @Builder.Default
    private List<ResourceOptionEntity> selectedResources = new ArrayList<>();

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}
