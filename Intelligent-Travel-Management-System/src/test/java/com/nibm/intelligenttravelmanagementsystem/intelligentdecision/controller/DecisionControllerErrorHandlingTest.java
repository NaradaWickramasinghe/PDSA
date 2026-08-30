package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nibm.intelligenttravelmanagementsystem.common.exception.GlobalExceptionHandler;
import com.nibm.intelligenttravelmanagementsystem.common.exception.MlInferenceException;
import com.nibm.intelligenttravelmanagementsystem.common.exception.ModelNotInitializedException;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.dto.RecommendationRequest;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.AgeGroup;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.TravelStyle;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.RecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DecisionController.class)
@Import(GlobalExceptionHandler.class)
class DecisionControllerErrorHandlingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RecommendationService recommendationService;

    private RecommendationRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = RecommendationRequest.builder()
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
                .build();
    }

    @Test
    @DisplayName("Negative budget should return HTTP 400 with validation failure message")
    void testNegativeBudget() throws Exception {
        validRequest.setBudget(new BigDecimal("-150.00"));

        mockMvc.perform(post("/api/decisions/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Budget must be greater than or equal to zero")));
    }

    @Test
    @DisplayName("Zero durationDays should return HTTP 400 with validation failure message")
    void testZeroDuration() throws Exception {
        validRequest.setDurationDays(0);

        mockMvc.perform(post("/api/decisions/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Duration must be at least 1 day")));
    }

    @Test
    @DisplayName("Zero groupSize should return HTTP 400 with validation failure message")
    void testZeroGroupSize() throws Exception {
        validRequest.setGroupSize(0);

        mockMvc.perform(post("/api/decisions/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Group size must be at least 1")));
    }

    @Test
    @DisplayName("Preference score > 10 should return HTTP 400 with validation message")
    void testPreferenceOutOfRange() throws Exception {
        validRequest.setAdventurePreference(25); // Invalid: > 10

        mockMvc.perform(post("/api/decisions/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Adventure preference must be between 1 and 10")));
    }

    @Test
    @DisplayName("Missing required fields (null budget) should return HTTP 400")
    void testMissingRequiredFields() throws Exception {
        validRequest.setBudget(null);

        mockMvc.perform(post("/api/decisions/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Budget is required")));
    }

    @Test
    @DisplayName("Malformed JSON payload should return HTTP 400 without leaking stack traces")
    void testMalformedJson() throws Exception {
        mockMvc.perform(post("/api/decisions/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid_json: true, budget: "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Malformed or unparseable JSON")));
    }

    @Test
    @DisplayName("ModelNotInitializedException should return HTTP 503 Service Unavailable")
    void testModelNotInitialized() throws Exception {
        when(recommendationService.getRecommendations(any(RecommendationRequest.class)))
                .thenThrow(new ModelNotInitializedException("Model uninitialized"));

        mockMvc.perform(post("/api/decisions/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("currently initializing")));
    }

    @Test
    @DisplayName("Unexpected ML runtime error should return HTTP 500 without leaking internal details")
    void testUnexpectedMlError() throws Exception {
        when(recommendationService.getRecommendations(any(RecommendationRequest.class)))
                .thenThrow(new MlInferenceException("Internal matrix dimension mismatch at /var/ml/engine.c"));

        mockMvc.perform(post("/api/decisions/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("/var/ml/engine.c"))))
                .andExpect(jsonPath("$.message").value("An error occurred while evaluating recommendations. Please verify your inputs and retry."));
    }
}
