package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "destinations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Destination {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank(message = "Destination name is required")
    @Size(max = 150, message = "Destination name must not exceed 150 characters")
    @Column(name = "name", length = 150, nullable = false, unique = true)
    private String name;

    @NotBlank(message = "Province is required")
    @Size(max = 100, message = "Province must not exceed 100 characters")
    @Column(name = "province", length = 100, nullable = false)
    private String province;

    @NotNull(message = "Average daily cost is required")
    @DecimalMin(value = "0.01", message = "Average daily cost must be greater than zero")
    @Column(name = "average_daily_cost", precision = 10, scale = 2, nullable = false)
    private BigDecimal averageDailyCost;

    @NotNull(message = "Minimum days is required")
    @Min(value = 1, message = "Minimum days must be at least 1")
    @Column(name = "minimum_days", nullable = false)
    private Integer minimumDays;

    @NotNull(message = "Maximum days is required")
    @Min(value = 1, message = "Maximum days must be at least 1")
    @Column(name = "maximum_days", nullable = false)
    private Integer maximumDays;

    @NotNull(message = "Beach score is required")
    @Min(value = 1, message = "Beach score must be between 1 and 10")
    @Max(value = 10, message = "Beach score must be between 1 and 10")
    @Column(name = "beach_score", nullable = false)
    private Integer beachScore;

    @NotNull(message = "Adventure score is required")
    @Min(value = 1, message = "Adventure score must be between 1 and 10")
    @Max(value = 10, message = "Adventure score must be between 1 and 10")
    @Column(name = "adventure_score", nullable = false)
    private Integer adventureScore;

    @NotNull(message = "Nature score is required")
    @Min(value = 1, message = "Nature score must be between 1 and 10")
    @Max(value = 10, message = "Nature score must be between 1 and 10")
    @Column(name = "nature_score", nullable = false)
    private Integer natureScore;

    @NotNull(message = "Culture score is required")
    @Min(value = 1, message = "Culture score must be between 1 and 10")
    @Max(value = 10, message = "Culture score must be between 1 and 10")
    @Column(name = "culture_score", nullable = false)
    private Integer cultureScore;

    @NotNull(message = "Nightlife score is required")
    @Min(value = 1, message = "Nightlife score must be between 1 and 10")
    @Max(value = 10, message = "Nightlife score must be between 1 and 10")
    @Column(name = "nightlife_score", nullable = false)
    private Integer nightlifeScore;

    @NotNull(message = "Relaxation score is required")
    @Min(value = 1, message = "Relaxation score must be between 1 and 10")
    @Max(value = 10, message = "Relaxation score must be between 1 and 10")
    @Column(name = "relaxation_score", nullable = false)
    private Integer relaxationScore;

    @NotNull(message = "Difficulty level is required")
    @Min(value = 1, message = "Difficulty level must be between 1 and 5")
    @Max(value = 5, message = "Difficulty level must be between 1 and 5")
    @Column(name = "difficulty_level", nullable = false)
    private Integer difficultyLevel;

    @Builder.Default
    @Column(name = "family_friendly", nullable = false)
    private Boolean familyFriendly = true;

    @Builder.Default
    @Column(name = "couple_friendly", nullable = false)
    private Boolean coupleFriendly = true;

    @Builder.Default
    @Column(name = "group_friendly", nullable = false)
    private Boolean groupFriendly = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (familyFriendly == null) familyFriendly = true;
        if (coupleFriendly == null) coupleFriendly = true;
        if (groupFriendly == null) groupFriendly = true;
    }
}
