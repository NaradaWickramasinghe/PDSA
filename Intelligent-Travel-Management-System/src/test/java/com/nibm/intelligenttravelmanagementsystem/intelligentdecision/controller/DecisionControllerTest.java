package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.dto.DestinationRecommendation;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.dto.RecommendationRequest;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.dto.RecommendationResponse;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.AgeGroup;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.SuitabilityLabel;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.TravelStyle;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.RecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DecisionController.class)
class DecisionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RecommendationService recommendationService;

    private RecommendationRequest validRequest;
    private RecommendationResponse mockResponse;

    @BeforeEach
    void setUp() {
        validRequest = RecommendationRequest.builder()
                .travelerId(UUID.randomUUID())
                .budget(new BigDecimal("800.00"))
                .durationDays(5)
                .groupSize(2)
                .ageGroup(AgeGroup.YOUNG_ADULT)
                .travelStyle(TravelStyle.ADVENTURE)
                .beachPreference(3)
                .adventurePreference(9)
                .naturePreference(8)
                .culturePreference(6)
                .nightlifePreference(4)
                .relaxationPreference(5)
                .topN(3)
                .build();

        DestinationRecommendation topRec = DestinationRecommendation.builder()
                .destination("Ella")
                .province("Uva")
                .rank(1)
                .score(0.92)
                .matchPercentage(92.0)
                .suitabilityLabel(SuitabilityLabel.EXCELLENT_FIT)
                .reason("Strong match for adventure and nature preferences, within budget and suitable for the requested duration.")
                .build();

        mockResponse = RecommendationResponse.builder()
                .travelerId(validRequest.getTravelerId())
                .totalCandidatesEvaluated(12)
                .recommendations(List.of(topRec))
                .decisionTreePrimaryPrediction("Ella")
                .summaryRationale("Top destination is Ella (Score: 92%).")
                .generatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/decisions/recommend with valid request should return HTTP 200 and ApiResponse payload")
    void testSuccessfulRecommendation() throws Exception {
        when(recommendationService.getRecommendations(any(RecommendationRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/decisions/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.recommendations[0].destination").value("Ella"))
                .andExpect(jsonPath("$.data.recommendations[0].score").value(0.92))
                .andExpect(jsonPath("$.data.recommendations[0].rank").value(1))
                .andExpect(jsonPath("$.data.recommendations[0].reason").exists());

        verify(recommendationService, times(1)).getRecommendations(any(RecommendationRequest.class));
    }

    @Test
    @DisplayName("POST /api/decisions/recommend with negative budget should return HTTP 400 Bad Request")
    void testValidationFailureNegativeBudget() throws Exception {
        validRequest.setBudget(new BigDecimal("-100.00")); // Invalid

        mockMvc.perform(post("/api/decisions/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(recommendationService);
    }

    @Test
    @DisplayName("POST /api/decisions/recommend with 0 durationDays should return HTTP 400 Bad Request")
    void testValidationFailureZeroDuration() throws Exception {
        validRequest.setDurationDays(0); // Invalid: min is 1

        mockMvc.perform(post("/api/decisions/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(recommendationService);
    }

    @Test
    @DisplayName("POST /api/decisions/recommend with out-of-range preference should return HTTP 400 Bad Request")
    void testValidationFailurePreferenceOutOfRange() throws Exception {
        validRequest.setAdventurePreference(15); // Invalid: max is 10

        mockMvc.perform(post("/api/decisions/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(recommendationService);
    }

    @Test
    @DisplayName("GET /api/decisions/health should return HTTP 200 OK")
    void testHealthCheck() throws Exception {
        mockMvc.perform(get("/api/decisions/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }
}
