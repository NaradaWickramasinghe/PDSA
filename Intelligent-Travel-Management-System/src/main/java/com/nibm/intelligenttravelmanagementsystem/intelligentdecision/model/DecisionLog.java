package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "decision_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class DecisionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "traveler_id")
    @ToString.Exclude
    private TravelerProfile traveler;

    @NotNull(message = "Destination is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_id", nullable = false)
    @ToString.Exclude
    private Destination destination;

    @NotNull(message = "Tree score is required")
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    @Column(name = "tree_score", nullable = false)
    private Float treeScore;

    @NotNull(message = "k-NN score is required")
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    @Column(name = "knn_score", nullable = false)
    private Float knnScore;

    @NotNull(message = "Final score is required")
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    @Column(name = "final_score", nullable = false)
    private Float finalScore;

    @NotNull(message = "Rank position is required")
    @Min(value = 1)
    @Column(name = "rank_position", nullable = false)
    private Integer rankPosition;

    @NotBlank(message = "Explanation is required")
    @Column(name = "explanation", columnDefinition = "TEXT", nullable = false)
    private String explanation;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
