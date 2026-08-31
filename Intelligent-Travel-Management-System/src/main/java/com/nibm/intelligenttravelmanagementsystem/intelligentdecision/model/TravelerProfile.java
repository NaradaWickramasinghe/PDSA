package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "traveler_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TravelerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "Age group is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "age_group", length = 30, nullable = false)
    private AgeGroup ageGroup;

    @NotNull(message = "Budget is required")
    @DecimalMin(value = "0.01", message = "Budget must be greater than zero")
    @Column(name = "budget", precision = 10, scale = 2, nullable = false)
    private BigDecimal budget;

    @NotNull(message = "Duration in days is required")
    @Min(value = 1, message = "Duration must be at least 1 day")
    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @NotNull(message = "Group size is required")
    @Min(value = 1, message = "Group size must be at least 1")
    @Column(name = "group_size", nullable = false)
    private Integer groupSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "travel_style", length = 50)
    private TravelStyle travelStyle;

    @NotNull(message = "Beach preference is required")
    @Min(value = 1, message = "Beach preference must be between 1 and 5")
    @Max(value = 5, message = "Beach preference must be between 1 and 5")
    @Column(name = "beach_preference", nullable = false)
    private Integer beachPreference;

    @NotNull(message = "Adventure preference is required")
    @Min(value = 1, message = "Adventure preference must be between 1 and 5")
    @Max(value = 5, message = "Adventure preference must be between 1 and 5")
    @Column(name = "adventure_preference", nullable = false)
    private Integer adventurePreference;

    @NotNull(message = "Nature preference is required")
    @Min(value = 1, message = "Nature preference must be between 1 and 5")
    @Max(value = 5, message = "Nature preference must be between 1 and 5")
    @Column(name = "nature_preference", nullable = false)
    private Integer naturePreference;

    @NotNull(message = "Culture preference is required")
    @Min(value = 1, message = "Culture preference must be between 1 and 5")
    @Max(value = 5, message = "Culture preference must be between 1 and 5")
    @Column(name = "culture_preference", nullable = false)
    private Integer culturePreference;

    @NotNull(message = "Nightlife preference is required")
    @Min(value = 1, message = "Nightlife preference must be between 1 and 5")
    @Max(value = 5, message = "Nightlife preference must be between 1 and 5")
    @Column(name = "nightlife_preference", nullable = false)
    private Integer nightlifePreference;

    @NotNull(message = "Relaxation preference is required")
    @Min(value = 1, message = "Relaxation preference must be between 1 and 5")
    @Max(value = 5, message = "Relaxation preference must be between 1 and 5")
    @Column(name = "relaxation_preference", nullable = false)
    private Integer relaxationPreference;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
