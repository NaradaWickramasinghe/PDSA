package com.nibm.intelligenttravelmanagementsystem.routesequencing.dto;

import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.dto.RecommendationRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ItineraryPlanRequest {

    @NotNull(message = "Traveler profile is required")
    @Valid
    private RecommendationRequest travelerProfile;

    @Builder.Default
    private int maxStops = 3;
}
