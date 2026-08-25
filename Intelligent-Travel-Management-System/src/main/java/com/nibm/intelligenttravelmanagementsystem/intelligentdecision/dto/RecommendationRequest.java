package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.dto;

import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.AgeGroup;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.TravelStyle;
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
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RecommendationRequest {

    private UUID travelerId;

    @NotNull(message = "Budget is required")
    @DecimalMin(value = "0.00", message = "Budget must be greater than or equal to zero")
    private BigDecimal budget;

    @NotNull(message = "Duration in days is required")
    @Min(value = 1, message = "Duration must be at least 1 day")
    private Integer durationDays;

    @NotNull(message = "Group size is required")
    @Min(value = 1, message = "Group size must be at least 1")
    private Integer groupSize;

    private AgeGroup ageGroup;
    private TravelStyle travelStyle;

    @NotNull(message = "Beach preference is required")
    @Min(value = 1, message = "Beach preference must be between 1 and 10")
    @Max(value = 10, message = "Beach preference must be between 1 and 10")
    private Integer beachPreference;

    @NotNull(message = "Adventure preference is required")
    @Min(value = 1, message = "Adventure preference must be between 1 and 10")
    @Max(value = 10, message = "Adventure preference must be between 1 and 10")
    private Integer adventurePreference;

    @NotNull(message = "Nature preference is required")
    @Min(value = 1, message = "Nature preference must be between 1 and 10")
    @Max(value = 10, message = "Nature preference must be between 1 and 10")
    private Integer naturePreference;

    @NotNull(message = "Culture preference is required")
    @Min(value = 1, message = "Culture preference must be between 1 and 10")
    @Max(value = 10, message = "Culture preference must be between 1 and 10")
    private Integer culturePreference;

    @NotNull(message = "Nightlife preference is required")
    @Min(value = 1, message = "Nightlife preference must be between 1 and 10")
    @Max(value = 10, message = "Nightlife preference must be between 1 and 10")
    private Integer nightlifePreference;

    @NotNull(message = "Relaxation preference is required")
    @Min(value = 1, message = "Relaxation preference must be between 1 and 10")
    @Max(value = 10, message = "Relaxation preference must be between 1 and 10")
    private Integer relaxationPreference;

    @Builder.Default
    private Integer topN = 5;

    private List<UUID> candidateDestinationIds;
}
