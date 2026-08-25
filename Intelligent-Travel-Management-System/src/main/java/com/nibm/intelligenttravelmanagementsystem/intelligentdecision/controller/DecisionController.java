package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.controller;

import com.nibm.intelligenttravelmanagementsystem.common.dto.ApiResponse;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.dto.RecommendationRequest;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.dto.RecommendationResponse;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.RecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(path = "/api/decisions", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class DecisionController {

    private final RecommendationService recommendationService;

    /**
     * Primary endpoint: Generates personalized travel recommendations based on traveler profile.
     */
    @PostMapping(path = "/recommend", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<RecommendationResponse>> getRecommendations(
            @Valid @RequestBody RecommendationRequest request
    ) {
        log.info("Received recommendation request for traveler: {}", request.getTravelerId());
        RecommendationResponse response = recommendationService.getRecommendations(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(response, "Recommendations generated successfully"));
    }

    /**
     * Health check endpoint for Module 4 Intelligent Decision subsystem.
     */
    @GetMapping(path = "/health")
    public ResponseEntity<ApiResponse<String>> checkHealth() {
        return ResponseEntity.ok(ApiResponse.success("Module 4 (Intelligent Decision Engine) is active and operational", "Health check OK"));
    }
}
